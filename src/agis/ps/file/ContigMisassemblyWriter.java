/** 
** Usage: TODO
** Author: mqin
** Email: mqin@outlook.com
** Date: 2017年7月14日
*/
package agis.ps.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agis.ps.link.MRecord;
import agis.ps.seqs.Sequence;
//import agis.ps.seqs.Contig;
import agis.ps.util.MisassemblyRegion;
import agis.ps.util.Parameter;

public class ContigMisassemblyWriter {
	public static Logger logger = LoggerFactory.getLogger(ContigMisassemblyWriter.class);
	private Parameter paras;
//	private Map<String, Contig> args;
	private Map<String, Sequence> seqs;
	
//	public ContigMisassemblyWriter(Parameter paras, Map<String, Sequence> seqs)
//	{
//		this.paras = paras;
//		this.seqs = seqs;
//	}
	
	public ContigMisassemblyWriter(Parameter paras) {
		this.paras = paras;
	}
	
	public void write(Map<String, Sequence> seqs) {
		String outFolder = paras.getOutFolder();
		String fileName = outFolder + System.getProperty("file.separator") + "contig_misassemblies.info";
		File file = null; 
		FileWriter fw = null;
		BufferedWriter bw = null;
		try
		{
			file = new File(fileName);
//			if (file.exists()) {
//				logger.info("The output file of scaffold is exist! It will not be overwrited!");
//				return;
//			}
			if(file.exists()) {
				logger.info("The output file " + file.getCanonicalPath() + " existed. It will overwrite.");
			} else  {
				if(!file.createNewFile())
				{
					logger.info("The output file " + file.getCanonicalPath() + "could not create.");
					return;
				}
			}
			fw = new FileWriter(file, false);
			bw = new BufferedWriter(fw);
			for(Map.Entry<String, Sequence> entry : seqs.entrySet())
			{
				List<MisassemblyRegion> regions = entry.getValue().getMisassemblies();
				for(MisassemblyRegion mr : regions)
				{
					bw.write(entry.getKey() + "\t" + mr.getStart() + "\t" +
							mr.getEnd() + "\t" + mr.getSupportLRs());
					bw.newLine();
				}
			}
			bw.flush();
			bw.close();
		} catch(IOException e)
		{
			logger.debug("Error: ", e);
			logger.error(this.getClass().getName() + "\t" + e.getMessage() + "\t" + e.getClass().getName());
		} finally
		{
			try{
				if(bw != null)
					bw.close();
			} catch(IOException e)
			{
				logger.debug("Error: ", e);
				logger.error(this.getClass().getName() + "\t" + e.getMessage() + "\t" + e.getClass().getName());
			}
		}
	}
	
}	
