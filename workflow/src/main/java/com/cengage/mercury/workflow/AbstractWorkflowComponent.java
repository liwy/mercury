package com.cengage.mercury.workflow;

import static java.lang.String.format;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The template of the workflow component, as all the workflow steps have the similar process pattern.
 * The workflow component that process each step need to extend this class
 *
 * @author wli
 *
 */
public abstract class AbstractWorkflowComponent implements MessageListener {
	private static final Logger logger = LoggerFactory.getLogger(AbstractWorkflowComponent.class);

	@Autowired
	protected RedisTemplate<String, String> redisTemp;
	@Autowired
	protected RabbitTemplate rabbitTemp;

	protected JdbcTemplate jdbcTemp;

	@Autowired
	public void setJdbcTemp(DataSource dataSource) {
		Assert.notNull(dataSource, "DataSource can't be null");
		this.jdbcTemp = new JdbcTemplate(dataSource);
	}

	@Override
	public final void onMessage(Message message) {
		// TODO may not need: find out workflow changed by admin thru UI

		// get item XML from cache
		String key = new String(message.getBody());
		String itemXML = retrieveCacheItem(key);

		// TODO what if the item missed from cache?

		// process item
		try {
			process(itemXML);
		} catch(Exception e) {
			logger.error(format("Failed to process article %s", key), e);
			logErrorToDB(e);
		}

		// send to next node, or finish it if last step
		moveToNextStep(message);
	}

	// TODO move it to common project, as it's needed by data receiver REST component
	public static final String WORK_FLOW_PATH = "workflowPath";

	/**
	 * Move the message to next node in the workflow path. Finalize if this is the last step.
	 * It only needs to update the "WORK_FLOW_PATH" header in the message and then resend it.
	 *
	 * @param message
	 */
	private void moveToNextStep(Message message) {
		String path = (String) message.getMessageProperties().getHeaders().get(WORK_FLOW_PATH);
		if(StringUtils.hasText(path)) {
			// TODO need to correct this to get the right queue name
			String nextQueueName = path.substring(0);



			// TODO the remaining part after removed nextNode
			String remainingPath = path.substring(0);
			message.getMessageProperties().setHeader(WORK_FLOW_PATH, remainingPath);

			rabbitTemp.send(nextQueueName, message);
		}
		else { // this is the last step
			// post-process ???
		}

	}

	/**
	 * This is where each step process the item with its own logic
	 *
	 * @param itemXML
	 */
	protected abstract void process(String itemXML);

	/**
	 * If failed to process the item, need to log into DB so that Admin can manually index it later on
	 *
	 * @param e
	 */
	private void logErrorToDB(Exception e) {
		// TODO jdbcTemp

	}

	/**
	 * Retrieve the item XML from the cache. Items are organized into hash
	 *
	 * @param key
	 * @return
	 */
	private String retrieveCacheItem(String key) {
		// TODO use redisTemp
		return null;
	}

}
