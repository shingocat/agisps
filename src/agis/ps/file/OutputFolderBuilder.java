/*
*File: agis.ps.file.OutputFolderBuilder.java
*User: mqin
*Email: mqin@ymail.com
*Date: 2016年7月19日
*/
package agis.ps.file;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agis.ps.util.Parameter;

public class OutputFolderBuilder {

	private static Logger logger = LoggerFactory.getLogger(OutputFolderBuilder.class);
	private Parameter paras;

	public OutputFolderBuilder(Parameter paras) {
		this.paras = paras;
	}

	public boolean building() {
		long start = System.currentTimeMillis();
		String path = paras.getOutFolder();
		boolean isValid = false;
		if (path == null || path.length() == 0) {
			logger.error("The output path was not setted!");
			return isValid;
		}
		try {
			File output = new File(path);
			if (output.exists()) {
				logger.info("The output folder existed!");
				isValid = true;
			} else {
				isValid = output.mkdirs();
				if (isValid)
					logger.info("Build output folder successfully!");
				else
					logger.info("Build output folder failed!");
			}
		} catch (SecurityException e) {
			logger.debug("Error: ", e);
			logger.error(this.getClass().getName() + e.getMessage() + "\t" + e.getClass().getName());
		} catch (Exception e) {
			logger.debug("Error: ", e);
			logger.error(this.getClass().getName() + e.getMessage() + "\t" + e.getClass().getName());
		}
		long end = System.currentTimeMillis();
		logger.info("Building output folder, elapsed time: " + (end - start) + " ms");
		return isValid;
	}

	/**
	 * only delete file under the output folder and do not create new output folder now;
	 * do not delete folder;
	 * @param dir
	 * @return
	 */
	private boolean deleteDir(File dir) {
		boolean success = false;
		if (dir.isDirectory()) {
			String[] children = dir.list();
			if(children.length == 0)
				success = true;
			for(String s : children)
			{
				File file = new File(dir,s);
				if(file.isFile())
				{
					success = file.delete();
					if(!success)
						break;
				}
			}
		}
		return success;
	}
}
