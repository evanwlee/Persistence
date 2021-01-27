package com.evanwlee.utils.storage;


import org.apache.log4j.Logger;

import com.evanwlee.data.MysqlPersistenceManager;
import com.evanwlee.utils.date.DateUtils;
import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.threading.work.IAsynchCommand;

public class CleanTransientDataAsynchCommand implements IAsynchCommand {
	private static Logger log = LoggerFactory.getLogger(CleanTransientDataAsynchCommand.class.getName());

	@Override
	public void execute() {
		String deleteQuery = "DELETE FROM transient_data_store WHERE expires < '"+DateUtils.format(DateUtils.dateNow(), "yyyy-MM-dd")+"'";
		log.info("Removing old Transient Data for this VM, query:"+deleteQuery);
		MysqlPersistenceManager.current().doModificationQuery(deleteQuery);

	}
}
