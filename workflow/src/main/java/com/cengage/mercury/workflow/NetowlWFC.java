/**
 *
 */
package com.cengage.mercury.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The queue listener that process the NetOwl
 *
 * @author wli
 *
 */
@Service("netowlWFC")
public class NetowlWFC extends AbstractWorkflowComponent {
	private static final Logger logger = LoggerFactory.getLogger(NetowlWFC.class);

	@Override
	public void process(String itemXML) {
		// TODO Auto-generated method stub

	}

}
