/*
*File: agis.ps.file.AlignmentFileReader.java
*User: mqin
*Email: mqin@ymail.com
*Date: 2017年1月11日
*/
package agis.ps.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agis.ps.link.MRecord;
import agis.ps.seqs.Contig;
import agis.ps.seqs.Scaffold;
import agis.ps.seqs.Sequence;
import agis.ps.util.MRecordValidator;
import agis.ps.util.MisassemblyChecker;
import agis.ps.util.Parameter;
import agis.ps.util.Strand;

public abstract class AlignmentFileReader {
	private final static Logger logger = LoggerFactory.getLogger(AlignmentFileReader.class);
	private String algFile;
	protected Parameter paras = null;
	private Map<String, Integer> seqCovs = null;
//	private Map<String, List<MRecord>> records = null;
//	private Map<String, Integer> cntLens = null;
	private List<List<MRecord>> listRecords = null;
//	private Map<String, Integer> samCntLens = null;
//	private Map<String, Contig> cnts;
//	private Map<String, Scaffold> scafs;
	private Map<String, Sequence> seqs;
//	private List<Contig> unusedCnts;
	
	public AlignmentFileReader(Parameter paras) {
		this.algFile = paras.getAlgFile();
		this.paras = paras;
	}
	
//	public AlignmentFileReader(Parameter paras, Map<String, Contig> cnts) {
//		this(paras);
//		this.cnts = cnts;
//	}
	
//	public List<List<MRecord>> readContigs(Map<String, Contig> cnts) {
//		this.cnts  = cnts;
//		return this.read();
//	}
//	
//	public List<List<MRecord>> readScaffolds(Map<String, Scaffold> scafs) {
//		this.scafs = scafs;
//		return this.readScafs();
//	}
	
	public List<List<MRecord>> read(Map<String, Sequence> seqs){
		this.seqs = seqs;
		return this.read();
	}
	
	private List<List<MRecord>> read() {
		long start = System.currentTimeMillis();
		File file = null;
		BufferedReader br = null;
		int lineIndex = 1; // for debug use;
		try {
			file = new File(algFile);
			br = Files.newBufferedReader(file.toPath());
			if(seqCovs == null)
				seqCovs = new HashMap<String, Integer>();
			if(listRecords == null) // build list to store record, assuming list size just for speeding up program.
				listRecords = new ArrayList<List<MRecord>>((int) (file.length()/100)); // 100 byte per line, not very strict
			seqCovs.clear();
			listRecords.clear();
			String line = null;
			String qId = "";
//			String maQid = ""; // same as qId, but for misassembly checking; 
			List<MRecord> rs = new ArrayList<MRecord>(); // aligned record
//			List<MRecord> maRs = new ArrayList<MRecord>(); // store all record for misassembly checking;
			while(true) {
				line = br.readLine();
				// the last line;
				if(line == null || line.isEmpty()) {
					if(rs.size() > 1)
						listRecords.add(rs);
//					if(!maRs.isEmpty())
//						misassemblyChecking(maRs);
					break;
				}
				line = line.trim();
				String [] arrs = line.split("\\s+");
				// if the M5 or m4 file format with header;
				if(arrs[0].equals("qName") && 
						(paras.getType().equalsIgnoreCase("m5") || paras.getType().equalsIgnoreCase("m4")))
					continue;
				if(arrs[0].startsWith("@") && paras.getType().equalsIgnoreCase("sam"))
					continue;
				// initiated Record
				MRecord record = initMRecord(arrs);
				if(record == null)
					continue;
				// misassembly checking;
//				if(record.getqName().equalsIgnoreCase(maQid))
//				{
//					maRs.add(record);
//				} else
//				{
//					if(!maRs.isEmpty())
//						misassemblyChecking(maRs);
//					maQid = record.getqName();
//					maRs = new ArrayList<MRecord>();
//					maRs.add(record); 
//				}
				// validate record to build link and check repeats;
//				logger.info("MRecordValidator on lines " + lineIndex + " start.");
				Integer seqLength = 0;
				if(this.seqs.containsKey(record.gettName())) {
					seqLength = this.seqs.get(record.gettName()).getLength();
				} else {
					br.close();
					throw new Exception("The draft assemblies do not have " + record.gettName() + " sequence.");
				}
				Map<String, Boolean> values = MRecordValidator.validate(record, paras, seqLength);
				logger.debug("MRecordValidator on lines " + lineIndex + " done.");
//				logger.info("Repeat finding on lines " + lineIndex + " start.");
				if(values.get("REPEAT")) { // only considering valid contigs to compute repeats;
					String tName = record.gettName();
					int minCntLen = paras.getMinContLen();
					if(seqLength >= minCntLen) {
						if(seqCovs.containsKey(tName)) {
							int count = seqCovs.get(tName) + 1;
							seqCovs.replace(tName, count);
						} else {
							seqCovs.put(tName, 1);
						}
					}
				}
				logger.debug("Repeat finding on lines " + lineIndex + " done.");
				// arraylist
//				logger.info("Record saving on lines " + lineIndex + " start.");
				if(values.get("RECORD")) {
					String qName = record.getqName();
					if(qName.equals(qId)) {
						rs.add(record);
					} else {
						if(rs.size() > 1)
							listRecords.add(rs);
						rs = new ArrayList<MRecord>();
						rs.add(record);
						qId = record.getqName();
					}
				} 
				logger.debug("Record saving on lines " + lineIndex + " done.");
				lineIndex++;
			}
			br.close();
		} catch(IOException e) {
			logger.debug("Error: ", e);
			logger.error(this.getClass().getName() + "\t" + e.getMessage());
		} catch (NumberFormatException e) {
			logger.debug("Error: ", e);
			logger.error(this.getClass().getName() + "\tOn lines: " + lineIndex  
					+ "\t" + e.getMessage());
		} catch(Exception e) {
			logger.debug("Error: ", e);
			logger.error(this.getClass().getName() + "\tOn lines: " + lineIndex 
					+ "\t" + e.getMessage());
		} finally {
			try{
				if(br != null)
					br.close();
			} catch(Exception e) {
				logger.debug("Error: ", e);
				logger.error(this.getClass().getName() + "\t" + e.getMessage());
			}
		}
		long end = System.currentTimeMillis();
//		logger.info("Valid Aligned Records: " + records.values().size());
		logger.info("Valid Aligned Records: " + listRecords.size());
		logger.info("Reading Aligned Records, elapsed time: " + (end - start) + " ms");
//		if(listRecords.isEmpty())
//		{
//			throw new IllegalArgumentException("The valid records in alignment file are empty! Please checking the alignment file!");
//		}
//		return records;
		return listRecords;
	}

	
//	private List<List<MRecord>> readScafs() {
//		long start = System.currentTimeMillis();
//		File file = null;
//		BufferedReader br = null;
//		int lineIndex = 1; // for debug use;
//		try {
//			file = new File(algFile);
//			br = Files.newBufferedReader(file.toPath());
//			if(seqCovs == null)
//				seqCovs = new HashMap<String, Integer>();
//			if(listRecords == null) // build list to store record, assuming list size just for speeding up program.
//				listRecords = new ArrayList<List<MRecord>>((int) (file.length()/100)); // 100 byte per line, not very strict
//			seqCovs.clear();
//			listRecords.clear();
//			String line = null;
//			String qId = "";
////			String maQid = ""; // same as qId, but for misassembly checking; 
//			List<MRecord> rs = new ArrayList<MRecord>(); // aligned record
////			List<MRecord> maRs = new ArrayList<MRecord>(); // store all record for misassembly checking;
//			while(true) {
//				line = br.readLine();
//				// the last line;
//				if(line == null || line.isEmpty()) {
//					if(rs.size() > 1)
//						listRecords.add(rs);
////					if(!maRs.isEmpty())
////						misassemblyChecking(maRs);
//					break;
//				}
//				line = line.trim();
//				String [] arrs = line.split("\\s+");
//				// if the M5 or m4 file format with header;
//				if(arrs[0].equals("qName") && 
//						(paras.getType().equalsIgnoreCase("m5") || paras.getType().equalsIgnoreCase("m4")))
//					continue;
//				if(arrs[0].startsWith("@") && paras.getType().equalsIgnoreCase("sam"))
//					continue;
//				// initiated Record
//				MRecord record = initMRecord(arrs);
//				if(record == null)
//					continue;
//				// misassembly checking;
////				if(record.getqName().equalsIgnoreCase(maQid))
////				{
////					maRs.add(record);
////				} else
////				{
////					if(!maRs.isEmpty())
////						misassemblyChecking(maRs);
////					maQid = record.getqName();
////					maRs = new ArrayList<MRecord>();
////					maRs.add(record); 
////				}
//				// validate record to build link and check repeats;
////				logger.info("MRecordValidator on lines " + lineIndex + " start.");
//				// original code 2020/9/16
////				Map<String, Boolean> values = MRecordValidator.validate(record, paras, cnts);
//				// now 
//				Integer seqLength = this.scafs.get(record.gettName()).getLength();
//				Map<String, Boolean> values = MRecordValidator.validate(record, paras, seqLength);
//				logger.debug("MRecordValidator on lines " + lineIndex + " done.");
////				logger.info("Repeat finding on lines " + lineIndex + " start.");
//				if(values.get("REPEAT")) { // only considering valid contigs to compute repeats;
//					String tName = record.gettName();
//					int minCntLen = paras.getMinContLen();
//					if(seqLength >= minCntLen) {
//						if(seqCovs.containsKey(tName)) {
//							int count = seqCovs.get(tName) + 1;
//							seqCovs.replace(tName, count);
//						} else {
//							seqCovs.put(tName, 1);
//						}
//					}
//				}
//				logger.debug("Repeat finding on lines " + lineIndex + " done.");
//				// arraylist
////				logger.info("Record saving on lines " + lineIndex + " start.");
//				if(values.get("RECORD")) {
//					String qName = record.getqName();
//					if(qName.equals(qId)) {
//						rs.add(record);
//					} else {
//						if(rs.size() > 1)
//							listRecords.add(rs);
//						rs = new ArrayList<MRecord>();
//						rs.add(record);
//						qId = record.getqName();
//					}
//				} 
//				logger.debug("Record saving on lines " + lineIndex + " done.");
//				lineIndex++;
//			}
//			br.close();
//		} catch(IOException e) {
//			logger.debug("Error: ", e);
//			logger.error(this.getClass().getName() + "\t" + e.getMessage());
//		} catch (NumberFormatException e) {
//			logger.debug("Error: ", e);
//			logger.error(this.getClass().getName() + "\tOn lines: " + lineIndex  
//					+ "\t" + e.getMessage());
//		} catch(Exception e)
//		{
//			logger.debug("Error: ", e);
//			logger.error(this.getClass().getName() + "\tOn lines: " + lineIndex 
//					+ "\t" + e.getMessage());
//		} finally {
//			try{
//				if(br != null)
//					br.close();
//			} catch(Exception e)
//			{
//				logger.debug("Error: ", e);
//				logger.error(this.getClass().getName() + "\t" + e.getMessage());
//			}
//		}
//		long end = System.currentTimeMillis();
////		logger.info("Valid Aligned Records: " + records.values().size());
//		logger.info("Valid Aligned Records: " + listRecords.size());
//		logger.info("Reading Aligned Records, erase time: " + (end - start) + " ms");
////		if(listRecords.isEmpty())
////		{
////			throw new IllegalArgumentException("The valid records in alignment file are empty! Please checking the alignment file!");
////		}
////		return records;
//		return listRecords;
//	}
	
//	public Map<String, List<MRecord>> read()
	// original code 2020/9/17
//	private List<List<MRecord>> read() {
//		long start = System.currentTimeMillis();
//		File file = null;
//		FileReader fr = null;
//		BufferedReader br = null;
//		int lineIndex = 1; // for debug use;
//		try
//		{
//			file = new File(algFile);
//			fr = new FileReader(file);
//			br = new BufferedReader(fr);
//			if(seqCovs == null)
//				seqCovs = new HashMap<String, Integer>();
//			if(listRecords == null) // build list to store record, assuming list size just for speeding up program.
//				listRecords = new ArrayList<List<MRecord>>((int) (file.length()/100)); // 100 byte per line, not very strict
//			seqCovs.clear();
//			listRecords.clear();
//			String line = null;
//			String qId = "";
////			String maQid = ""; // same as qId, but for misassembly checking; 
//			List<MRecord> rs = new ArrayList<MRecord>(); // aligned record
////			List<MRecord> maRs = new ArrayList<MRecord>(); // store all record for misassembly checking;
//			while(true)
//			{
//				line = br.readLine();
//				// the last line;
//				if(line == null || line.isEmpty())
//				{
//					if(rs.size() > 1)
//						listRecords.add(rs);
////					if(!maRs.isEmpty())
////						misassemblyChecking(maRs);
//					break;
//				}
//				line = line.trim();
//				String [] arrs = line.split("\\s+");
//				// if the M5 or m4 file format with header;
//				if(arrs[0].equals("qName") && 
//						(paras.getType().equalsIgnoreCase("m5") || paras.getType().equalsIgnoreCase("m4")))
//					continue;
//				if(arrs[0].startsWith("@") && paras.getType().equalsIgnoreCase("sam"))
//					continue;
//				// initiated Record
//				MRecord record = initMRecord(arrs);
//				if(record == null)
//					continue;
//				// misassembly checking;
////				if(record.getqName().equalsIgnoreCase(maQid))
////				{
////					maRs.add(record);
////				} else
////				{
////					if(!maRs.isEmpty())
////						misassemblyChecking(maRs);
////					maQid = record.getqName();
////					maRs = new ArrayList<MRecord>();
////					maRs.add(record); 
////				}
//				// validate record to build link and check repeats;
////				logger.info("MRecordValidator on lines " + lineIndex + " start.");
//				Map<String, Boolean> values = MRecordValidator.validate(record, paras, cnts);
//				logger.debug("MRecordValidator on lines " + lineIndex + " done.");
////				logger.info("Repeat finding on lines " + lineIndex + " start.");
//				if(values.get("REPEAT"))
//				{ // only considering valid contigs to compute repeats;
//					String tName = record.gettName();
//					int cLen = cnts.get(tName).getLength();
//					int minCntLen = paras.getMinContLen();
//					if(cLen >= minCntLen)
//					{
//						if(seqCovs.containsKey(tName))
//						{
//							int count = seqCovs.get(tName) + 1;
//							seqCovs.replace(tName, count);
//						} else
//						{
//							seqCovs.put(tName, 1);
//						}
//					}
//				}
//				logger.debug("Repeat finding on lines " + lineIndex + " done.");
//				// arraylist
////				logger.info("Record saving on lines " + lineIndex + " start.");
//				if(values.get("RECORD"))
//				{
//					String qName = record.getqName();
//					if(qName.equals(qId))
//					{
//						rs.add(record);
//					} else
//					{
//						if(rs.size() > 1)
//							listRecords.add(rs);
//						rs = new ArrayList<MRecord>();
//						rs.add(record);
//						qId = record.getqName();
//					}
//				} 
//				logger.debug("Record saving on lines " + lineIndex + " done.");
//				lineIndex++;
//			}
//			br.close();
//		} catch(IOException e)
//		{
//			logger.debug("Error: ", e);
//			logger.error(this.getClass().getName() + "\t" + e.getMessage());
//		} catch (NumberFormatException e) {
//			logger.debug("Error: ", e);
//			logger.error(this.getClass().getName() + "\tOn lines: " + lineIndex  
//					+ "\t" + e.getMessage());
//		} catch(Exception e)
//		{
//			logger.debug("Error: ", e);
//			logger.error(this.getClass().getName() + "\tOn lines: " + lineIndex 
//					+ "\t" + e.getMessage());
//		} finally
//		{
//			try{
//				if(br != null)
//					br.close();
//			} catch(Exception e)
//			{
//				logger.debug("Error: ", e);
//				logger.error(this.getClass().getName() + "\t" + e.getMessage());
//			}
//		}
//		long end = System.currentTimeMillis();
////		logger.info("Valid Aligned Records: " + records.values().size());
//		logger.info("Valid Aligned Records: " + listRecords.size());
//		logger.info("Reading Aligned Records, erase time: " + (end - start) + " ms");
////		if(listRecords.isEmpty())
////		{
////			throw new IllegalArgumentException("The valid records in alignment file are empty! Please checking the alignment file!");
////		}
////		return records;
//		return listRecords;
//	}

	protected abstract MRecord initMRecord(String arrs []);
	
	public Map<String, Integer> getCntCoverages() {
		return this.seqCovs;
	}
	
//	public Map<String, Integer> getCntLengths()
//	{
//		return this.cntLens;
//	}
	
	public List<List<MRecord>> getListRecord() {
		return this.listRecords;
	}
	
	/*private void misassemblyChecking(List<MRecord> maRs)
	{
		if(maRs == null || maRs.isEmpty())
			return;
		int size = maRs.size();
		double threshold = 0.5;
		// method 4
		MisassemblyChecker.checking2(paras, cnts, maRs);
//		// method 3: checking whether the long reads including repeat regions;
//		if(size == 1)
//		{
//			MisassemblyChecker.checking(paras, cnts, maRs.get(0));
//		} else
//		{
//			boolean isExist = false;
//		outer:	for(int i = 0; i < size; i++)
//			{
//				MRecord target = maRs.get(i);
//				for(int j = 0; j < size; j++)
//				{
//					if(j != i)
//					{
//						int tStart = target.getqStart();
//						int tEnd = target.getqEnd();
//						int tRegionLen = tEnd - tStart;
//						MRecord query = maRs.get(j);
//						int qStart = query.getqStart();
//						int qEnd = query.getqEnd();
//						// two points in the middle define overlap region
//						if(qStart < tEnd && qEnd > tStart)
//						{
//							int olRegionLen = 0;
//							int [] temp = {tStart, tEnd, qStart, qEnd};
//							Arrays.sort(temp);
//							olRegionLen = temp[2] - temp[1];
//							double tOLRatio = (double)olRegionLen / tRegionLen;
//							if(tOLRatio > threshold)
//							{
//								isExist = true;
//								break outer;
//							}
//						}
//					}
//				}
//			}
//			if(!isExist)
//			{
//				for(int i = 0; i < size; i++)
//					MisassemblyChecker.checking(paras, cnts, maRs.get(i));
//			}
//		}
		// method 2: checking whether overlap among all contigs;
//		double threshold = 0.1;
//		if(maRs.get(0).getqName().equals("623L15891/0_15891"))
//			logger.debug("breakpoint");
//		if(size == 1)
//		{
//			MisassemblyChecker.checking(paras, cnts, maRs.get(0));
//		} else
//		{
//			for(int i = 0; i < size; i++)
//			{
//				MRecord target = maRs.get(i);
//				boolean isExist = false;
//				for(int j = 0; j < size; j++)
//				{ // checking weather there are overlap regions.
//					if(j != i)
//					{
//						int tStart = target.getqStart();
//						int tEnd = target.getqEnd();
//						int tRegionLen = tEnd - tStart;
//						MRecord query = maRs.get(j);
//						int qStart = query.getqStart();
//						int qEnd = query.getqEnd();
//						// two points in the middle define overlap region
//						if(qStart < tEnd && qEnd > tStart)
//						{
//							int olRegionLen = 0;
//							int [] temp = {tStart, tEnd, qStart, qEnd};
//							Arrays.sort(temp);
//							olRegionLen = temp[2] - temp[1];
//							double tOLRatio = (double)olRegionLen / tRegionLen;
//							if(tOLRatio > threshold)
//							{
//								isExist = true;
//								break;
//							}
//						}
//					} 
//				}
//				if(!isExist)
//					MisassemblyChecker.checking(paras, cnts, target);
//			}
//		}
		// method 1: checking whether overlap between two adjacent contigs;
//		MRecord target = maRs.get(0);
//		if(target.getqName().equals("483L5351/0_5351"))
//			logger.info("breakpoint");
//		if(size == 1)
//		{
//			MisassemblyChecker.checking(paras, cnts, target);
//		} else
//		{
//			boolean isTargetCheck = true;
//			for(int i = 1; i < size; i++)
//			{
//				int tStart = target.getqStart();
//				int tEnd = target.getqEnd();
//				int tRegionLen = tEnd - tStart;
//				MRecord query = maRs.get(i);
//				int qStart = query.getqStart();
//				int qEnd = query.getqEnd();
//				int qRegionLen = qEnd - qStart;
//				int olRegionLen = 0;
//				// two points in the middle define overlap region
//				if(qStart < tEnd && qEnd > tStart)
//				{
//					int [] temp = {tStart, tEnd, qStart, qEnd};
//					Arrays.sort(temp);
//					olRegionLen = temp[2] - temp[1];
//				}
//				double tOLRatio = (double)olRegionLen / tRegionLen;
//				double qOLRatio = (double)olRegionLen / qRegionLen;
//				double threshold = 0.7;
//				if(tOLRatio <= threshold)
//				{
//					if(isTargetCheck)
//						MisassemblyChecker.checking(paras, cnts, target);
//					target = query;
//					if(qOLRatio >= threshold) 
//						isTargetCheck = false;
//					else
//						isTargetCheck = true;
//				} else
//				{
//					target = query;
//					if(qOLRatio >= threshold) 
//						isTargetCheck = false;
//					else
//						isTargetCheck = true;
//				}
//				
//				if(i == (size - 1) && isTargetCheck)
//				{
//					MisassemblyChecker.checking(paras, cnts, target);
//				}
//			}
//		}
	}*/
	
	
//	// print in m4 format
//	private void pritnMRecord(MRecord record){
//		System.out.println(record.getqName() + "\t" + record.gettName() + "\t" + "-1000" + "\t" +
//				record.getIdentity() + "\t" + "0" + record.getqStart() + "\t" + record.getqEnd() + "\t" +
//				record.getqLength() + "\t" + (record.gettStart().equals(Strand.FORWARD)?"0":"1") + "\t" +
//				record.gettStart() + "\t" + record.gettEnd() + "\t" + record.gettLength() + "\t" + 255);
//	}
}


