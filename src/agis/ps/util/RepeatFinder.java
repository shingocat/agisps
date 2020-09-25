/*
*File: agis.ps.util.RepeatFinder.java
*User: mqin
*Email: mqin@ymail.com
*Date: 2016年5月12日
*/
package agis.ps.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agis.ps.file.SequenceCoverageWriter;
//import agis.ps.seqs.Contig;
//import agis.ps.seqs.Scaffold;

public class RepeatFinder {

	public static Logger logger = LoggerFactory.getLogger(RepeatFinder.class);
	private Parameter paras;
	private List<String> repeats;
	private double iqrTime;
//	private Map<String, Contig> cnts;
//	private Map<String, Scaffold> scafs;
	// private Map<String, List<MRecord>> cntMaps;

	public RepeatFinder(Parameter paras) {
		this.paras = paras;
		iqrTime = paras.getIqrTime();
	}

	// public List<String> findRepeats(Map<String, Integer> cntCovs)
	// {
	// long start = System.currentTimeMillis();
	// List<Integer> covs = new ArrayList<Integer>(cntCovs.values());
	// int mean = MathTool.mean(covs);
	// int sd = MathTool.sd(covs);
	// int median = (int) MathTool.median(covs);
	// int upper = median + 2 * sd;
	// logger.info("Mean cov = " + mean);
	// logger.info("Median cov = " + median);
	// logger.info("S.D. = " + sd);
	// logger.info("Mean Range 95%: [" + (mean - 2 * sd) + " : " + (mean + 2 *
	// sd) + "]");
	// logger.info("Mean Range 99%: [" + (mean - 3 * sd) + " : " + (mean + 3 *
	// sd) + "]");
	// logger.info("Median Range 95%: [" + (median - 2 * sd) + " : " + (median +
	// 2 * sd) + "]");
	// logger.info("Median Range 99%: [" + (median - 3 * sd) + " : " + (median +
	// 3 * sd) + "]");
	//// logger.info("Pesudo repeat contigs");
	//// Map<String, List<String>> repeat = new HashMap<String, List<String>>();
	// if(repeats == null)
	// repeats = new Vector<String>(30);
	// for(String id : cntCovs.keySet())
	// {
	// if(cntCovs.get(id) > upper)
	// {
	// repeats.add(id);
	// }
	// }
	// logger.info("Repeat count: " + repeats.size());
	// ContigCoverageWriter ccw = new ContigCoverageWriter(paras);
	// ccw.write2(cntCovs);
	// long end = System.currentTimeMillis();
	// logger.info("Finding repeat, erase time: " + (end - start) + " ms");
	// return repeats;
	// }
	//

//	public List<String> findRepeats(Map<String, Integer> seqCovs, Map<String, Contig> cnts) {
//		this.cnts = cnts;
//		return this.findRepeats(seqCovs);
//	}

	public List<String> findRepeats(Map<String, Integer> seqCovs) {
		long start = System.currentTimeMillis();
		// List<String>>();
		if (repeats == null)
			repeats = new ArrayList<String>(seqCovs.size()/5);
		if (!seqCovs.isEmpty()) {
			List<Integer> covs = new ArrayList<Integer>(seqCovs.values());
			// int mean = MathTool.mean(covs);
			// int sd = MathTool.sd(covs);
			Map<String, Double> values = MathTool.summary(covs);
			double firstQ = values.get("FIRSTQ");
			double thirdQ = values.get("THIRDQ");
			double iqr = thirdQ - firstQ;
			double upper = iqr * iqrTime + values.get("THIRDQ");
			double median = values.get("MEDIAN");
			logger.info("Finding Repeats:");
			logger.info("MIN: " + values.get("MIN"));
			logger.info("First Quartile: " + firstQ);
			// logger.info("Mean cov = " + mean);
			logger.info("Median cov = " + median);
			// logger.info("S.D. = " + sd);
			// logger.info("Mean Range 95%: [" + (mean - 2 * sd) + " : " + (mean
			// + 2 * sd) + "]");
			// logger.info("Mean Range 99%: [" + (mean - 3 * sd) + " : " + (mean
			// + 3 * sd) + "]");
			// logger.info("Median Range 95%: [" + (median - 2 * sd) + " : " +
			// (median + 2 * sd) + "]");
			// logger.info("Median Range 99%: [" + (median - 3 * sd) + " : " +
			// (median + 3 * sd) + "]");
			logger.info("Third Quartile: " + thirdQ);
			logger.info("MAX: " + values.get("MAX"));
			logger.info("Interquartile Range: " + iqr);
			logger.info(iqrTime + "'s IQR " + ", Outlier Threshold: " + upper);
			// logger.info("Pesudo repeat contigs");
			// Map<String, List<String>> repeat = new HashMap<String,
			for (String id : seqCovs.keySet()) {
				if (seqCovs.get(id) > upper) {
					repeats.add(id);
					// oringal code 2020/9/16
//					if (cnts.containsKey(id))
//						cnts.get(id).setIsRepeat(true);
				}
			}
		}
		logger.info("Repeat count: " + repeats.size());
		long end = System.currentTimeMillis();
		logger.info("Finding repeat, elapsed time: " + (end - start) + " ms");
		SequenceCoverageWriter ccw = new SequenceCoverageWriter(paras);
		ccw.write2(seqCovs);
		return repeats;
	}

	// public List<String> findRepeat2()
	// {
	// long start = System.currentTimeMillis();
	// if(cntMaps == null)
	// cntMaps = new HashMap<String, List<MRecord>>();
	// cntMaps.clear();
	// File file = null;
	// FileReader fr = null;
	// BufferedReader br = null;
	// String alnFile = paras.getAlgFile();
	// try {
	// file = new File(alnFile);
	// if (!file.exists()) {
	// logger.error(this.getClass().getName() + "\t" + "The m5 file do not
	// exist!");
	// return null;
	// }
	// fr = new FileReader(file);
	// br = new BufferedReader(fr);
	// String line = null;
	// String[] arrs = null;
	// while(true)
	// {
	// line = br.readLine();
	// if(line != null)
	// {
	// arrs = line.split("\\s+");
	// if (arrs[0].equalsIgnoreCase("qName") &&
	// arrs[1].equalsIgnoreCase("qLength"))
	// continue;
	// MRecord m = MRecordValidator.validate4Repeats(arrs, paras);
	// if(m != null)
	// {
	// String cntId = m.gettName();
	// if(cntMaps.containsKey(cntId))
	// {
	// List<MRecord> ms = cntMaps.get(cntId);
	// if(!ms.contains(m))
	// {
	// ms.add(m);
	// cntMaps.replace(cntId, ms);
	// }
	// } else
	// {
	// List<MRecord> ms = new Vector<MRecord>(10);
	// ms.add(m);
	// cntMaps.put(cntId, ms);
	// }
	// }
	//
	// } else
	// {
	// break;
	// }
	// }
	// } catch(IOException e)
	// {
	// logger.error(this.getClass().getName() + "\t" + e.getMessage());
	// } catch(Exception e)
	// {
	// logger.error(this.getClass().getName() + "\t" + e.getMessage());
	// } finally
	// {
	// try{
	// if(br != null)
	// br.close();
	// } catch(Exception e)
	// {
	// logger.error(this.getClass().getName() + "\t" + e.getMessage());
	// }
	// }
	//
	// List<Integer> covs = new Vector<Integer>();
	// for(String id : cntMaps.keySet())
	// {
	//// logger.debug("id = " + id + ", cov = " + cntCounts.get(id).size());
	// covs.add(cntMaps.get(id).size());
	// }
	// int mean = MathTool.mean(covs);
	// int sd = MathTool.sd(covs);
	// int median = MathTool.median(covs);
	// int upper = median + 2 * sd;
	// logger.info("Mean cov = " + mean);
	// logger.info("Median cov = " + median);
	// logger.info("S.D. = " + sd);
	// logger.info("Mean Range 95%: [" + (mean - 2 * sd) + " : " + (mean + 2 *
	// sd) + "]");
	// logger.info("Mean Range 99%: [" + (mean - 3 * sd) + " : " + (mean + 3 *
	// sd) + "]");
	// logger.info("Median Range 95%: [" + (median - 2 * sd) + " : " + (median +
	// 2 * sd) + "]");
	// logger.info("Median Range 99%: [" + (median - 3 * sd) + " : " + (median +
	// 3 * sd) + "]");
	// logger.info("Pesudo repeat contigs");
	//// Map<String, List<String>> repeat = new HashMap<String, List<String>>();
	// if(repeats == null)
	// repeats = new Vector<String>(30);
	// for(String id : cntMaps.keySet())
	// {
	// if(cntMaps.get(id).size() > upper)
	// {
	//// repeat.put(id, cntCounts.get(id));
	// repeats.add(id);
	// logger.info(id + " might be repeat!");
	// }
	// }
	// logger.info("Repeat count: " + repeats.size());
	//// ContigCoverageWriter ccw = new ContigCoverageWriter(paras, cntMaps);
	// ContigCoverageWriter ccw = new ContigCoverageWriter(paras);
	// ccw.write(cntMaps);
	// long end = System.currentTimeMillis();
	// logger.info("Finding repeat, erase time: " + (end - start) + " ms");
	// return repeats;
	// }
	//
	// @SuppressWarnings("unused")
	// private static Map<String, List<MRecord>> sortByComparator(Map<String,
	// List<MRecord>> unsortMap, final boolean order)
	// {
	//
	// List<Entry<String, List<MRecord>>> list = new LinkedList<Entry<String,
	// List<MRecord>>>(unsortMap.entrySet());
	//
	// // Sorting the list based on values
	// Collections.sort(list, new Comparator<Entry<String, List<MRecord>>>()
	// {
	// @Override
	// public int compare(Entry<String, List<MRecord>> o1, Entry<String,
	// List<MRecord>> o2) {
	// if (order)
	// {
	// int o1Size = o1.getValue().size();
	// int o2Size = o2.getValue().size();
	// if(o1Size >= o2Size)
	// return 1;
	// else
	// return -1;
	// }
	// else
	// {
	// int o1Size = o1.getValue().size();
	// int o2Size = o2.getValue().size();
	// if(o2Size >= o1Size)
	// return 1;
	// else
	// return -1;
	// }
	// }
	// });
	//
	// // Maintaining insertion order with the help of LinkedList
	// Map<String, List<MRecord>> sortedMap = new LinkedHashMap<String,
	// List<MRecord>>();
	// for (Entry<String, List<MRecord>> entry : list)
	// {
	// sortedMap.put(entry.getKey(), entry.getValue());
	// }
	//
	// return sortedMap;
	// }

	// public List<String> findRepeat()
	// {
	// long start = System.currentTimeMillis();
	// if(cntMaps == null)
	// cntMaps = new HashMap<String, List<MRecord>>();
	// cntMaps.clear();
	// File file = null;
	// FileReader fr = null;
	// BufferedReader br = null;
	// String alnFile = paras.getAlgFile();
	// try {
	// file = new File(alnFile);
	// if (!file.exists()) {
	// logger.error(this.getClass().getName() + "\t" + "The m5 file do not
	// exist!");
	// return null;
	// }
	// fr = new FileReader(file);
	// br = new BufferedReader(fr);
	// String line = null;
	// String[] arrs = null;
	// while(true)
	// {
	// line = br.readLine();
	// if(line != null)
	// {
	// arrs = line.split("\\s+");
	// if (arrs[0].equalsIgnoreCase("qName") &&
	// arrs[1].equalsIgnoreCase("qLength"))
	// continue;
	// MRecord m = MRecordValidator.validate4Repeats(arrs, paras);
	// if(m != null)
	// {
	// String pbId = m.getqName();
	// if(cntMaps.containsKey(pbId))
	// {
	// List<MRecord> ms = cntMaps.get(pbId);
	// if(!ms.contains(m))
	// {
	// ms.add(m);
	// cntMaps.replace(pbId, ms);
	// }
	// } else
	// {
	// List<MRecord> ms = new Vector<MRecord>(10);
	// ms.add(m);
	// cntMaps.put(pbId, ms);
	// }
	// }
	// } else
	// {
	// break;
	// }
	// }
	// } catch(IOException e)
	// {
	// logger.error(this.getClass().getName() + "\t" + e.getMessage());
	// } catch(Exception e)
	// {
	// logger.error(this.getClass().getName() + "\t" + e.getMessage());
	// } finally
	// {
	// try{
	// if(br != null)
	// br.close();
	// } catch(Exception e)
	// {
	// logger.error(this.getClass().getName() + "\t" + e.getMessage());
	// }
	// }
	// repeats = this.findRepeat(cntMaps);
	// long end = System.currentTimeMillis();
	// logger.info("Finding repeat, erase time: " + (end - start) + " ms");
	// return repeats;
	// }

	// public List<String> findRepeat(Map<String, List<MRecord>> args)
	// {
	// List<String> repeats = new Vector<String>(30);
	// // contig id and pacbio id in List<String>
	// Map<String, List<String>> cntCounts = new HashMap<String,
	// List<String>>();
	// for(String s : args.keySet())
	// {
	// List<MRecord> cnts = args.get(s);
	// for(MRecord m : cnts)
	// {
	// if(cntCounts.containsKey(m.gettName()))
	// {
	// List<String> temp = cntCounts.get(m.gettName());
	// int tLen = m.gettLength();
	// int tStart = m.gettStart();
	// int tEnd = m.gettEnd();
	// int tLeftLen = tStart;
	// int tRightLen = tLen - tEnd;
	// int maxOHLen = paras.getMaxOHLen();
	// int defOHLen = (int) (tLen * paras.getMaxOHRatio());
	// if(maxOHLen <= defOHLen)
	// defOHLen = maxOHLen;
	// if(tLeftLen > defOHLen )
	// continue;
	// if(tRightLen > defOHLen)
	// continue;
	// if(!temp.contains(s))
	// temp.add(s);
	// } else
	// {
	// Vector<String> temp = new Vector<String>(30);
	// int tLen = m.gettLength();
	// int tStart = m.gettStart();
	// int tEnd = m.gettEnd();
	// int tLeftLen = tStart;
	// int tRightLen = tLen - tEnd;
	// int maxOHLen = paras.getMaxOHLen();
	// int defOHLen = (int) (tLen * paras.getMaxOHRatio());
	// if(maxOHLen <= defOHLen)
	// defOHLen = maxOHLen;
	// if(tLeftLen > defOHLen )
	// continue;
	// if(tRightLen > defOHLen)
	// continue;
	// temp.add(s);
	// cntCounts.put(m.gettName(), temp);
	// temp = null;
	// }
	// }
	// }
	// List<Integer> covs = new Vector<Integer>();
	// for(String id : cntCounts.keySet())
	// {
	//// logger.debug("id = " + id + ", cov = " + cntCounts.get(id).size());
	// covs.add(cntCounts.get(id).size());
	// }
	// int mean = MathTool.mean(covs);
	// int sd = MathTool.sd(covs);
	// int median = MathTool.median(covs);
	// int upper = median + 2 * sd;
	// logger.info("Mean cov = " + mean);
	// logger.info("Median cov = " + median);
	// logger.info("S.D. = " + sd);
	// logger.info("Mean Range 95%: [" + (mean - 2 * sd) + " : " + (mean + 2 *
	// sd) + "]");
	// logger.info("Mean Range 99%: [" + (mean - 3 * sd) + " : " + (mean + 3 *
	// sd) + "]");
	// logger.info("Median Range 95%: [" + (median - 2 * sd) + " : " + (median +
	// 2 * sd) + "]");
	// logger.info("Median Range 99%: [" + (median - 3 * sd) + " : " + (median +
	// 3 * sd) + "]");
	// logger.info("Pesudo repeat contigs");
	//// Map<String, List<String>> repeat = new HashMap<String, List<String>>();
	// for(String id : cntCounts.keySet())
	// {
	// if(cntCounts.get(id).size() > upper)
	// {
	//// repeat.put(id, cntCounts.get(id));
	// repeats.add(id);
	// logger.info(id + " might be repeat!");
	// }
	// }
	// logger.info("Repeat count: " + repeats.size());
	// ContigCoverageWriter ccw = new ContigCoverageWriter(paras, cntCounts);
	// ccw.write();;
	// return repeats;
	// }

	// public List<String> findByFileEncapsulate(M5FileEncapsulate m5file) {
	// long start = System.currentTimeMillis();
	// if(cntMaps == null)
	// cntMaps = new HashMap<String, List<MRecord>>();
	// cntMaps.clear();
	// int index = 0;
	// M5Record m5 = null;
	// try {
	// while(true)
	// {
	// m5 = m5file.getM5Record(index);
	// if(m5 != null)
	// {
	// MRecord m = MRecordValidator.validate4Repeats(m5, paras);
	// if(m != null)
	// {
	// String cntId = m.gettName();
	// if(cntMaps.containsKey(cntId))
	// {
	// List<MRecord> ms = cntMaps.get(cntId);
	// if(!ms.contains(m))
	// {
	// ms.add(m);
	// cntMaps.replace(cntId, ms);
	// }
	// } else
	// {
	// List<MRecord> ms = new Vector<MRecord>(10);
	// ms.add(m);
	// cntMaps.put(cntId, ms);
	// }
	// }
	// index++;
	// } else
	// {
	// break;
	// }
	// }
	// } catch(Exception e)
	// {
	// logger.error(this.getClass().getName() + "\t" + e.getMessage());
	// }
	//
	// List<Integer> covs = new Vector<Integer>();
	// for(String id : cntMaps.keySet())
	// {
	// covs.add(cntMaps.get(id).size());
	// }
	// int mean = MathTool.mean(covs);
	// int sd = MathTool.sd(covs);
	// int median = MathTool.median(covs);
	// int upper = median + 2 * sd;
	// logger.info("Mean cov = " + mean);
	// logger.info("Median cov = " + median);
	// logger.info("S.D. = " + sd);
	// logger.info("Mean Range 95%: [" + (mean - 2 * sd) + " : " + (mean + 2 *
	// sd) + "]");
	// logger.info("Mean Range 99%: [" + (mean - 3 * sd) + " : " + (mean + 3 *
	// sd) + "]");
	// logger.info("Median Range 95%: [" + (median - 2 * sd) + " : " + (median +
	// 2 * sd) + "]");
	// logger.info("Median Range 99%: [" + (median - 3 * sd) + " : " + (median +
	// 3 * sd) + "]");
	// logger.info("Pesudo repeat contigs");
	// if(repeats == null)
	// repeats = new Vector<String>(30);
	// //do not print this info
	// for(String id : cntMaps.keySet())
	// {
	// if(cntMaps.get(id).size() > upper)
	// {
	// repeats.add(id);
	//// logger.info(id + " might be repeat!");
	// }
	// }
	// logger.info("Repeat count: " + repeats.size());
	// ContigCoverageWriter ccw = new ContigCoverageWriter(paras);
	// ccw.write(cntMaps);
	// long end = System.currentTimeMillis();
	// logger.info("Finding repeat, erase time: " + (end - start) + " ms");
	// return repeats;
	// }
}
