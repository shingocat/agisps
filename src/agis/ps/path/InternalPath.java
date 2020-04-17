/*
*File: agis.ps.path.InternalPath.java
*User: mqin
*Email: mqin@ymail.com
*Date: 2016年12月22日
*/
package agis.ps.path;

import java.util.LinkedList;

import agis.ps.seqs.Contig;

public class InternalPath {
	private LinkedList<Contig> path = new LinkedList<Contig>(); // the Linked contig list for this internal path
	private int score = 0; // this path score
	
	public void addFirst(Contig fst)
	{
		path.addFirst(fst);
	}
	
	public void addLast(Contig lst)
	{
		path.addLast(lst);
	}
	
	public void addScore(int score)
	{
		this.score += score;
	}
	
	public void subtractScore(int score)
	{
		this.score -= score;
	}
	
	public int getScore()
	{
		return this.score;
	}
	
	/**
	 * path is contain the divergence point as the first contig;
	 * but it should not be considering this one;
	 * so if there is no such element or index == 0, it will 
	 * return false;
	 * @param c
	 * @return
	 */
	public boolean isContain(Contig c)
	{
		return path.contains(c);
//		int index = path.indexOf(c);
//		if(index == -1 || index == 0)
//			return false;
//		else 
//			return true;
	}
	
	public Contig getCnt(int index)
	{
		if(index < 0 || index >= path.size())
			return null;
		return path.get(index);
	}
	
	public int getIndex(Contig c)
	{
		return path.indexOf(c);
	}
	
	public boolean isEmpty()
	{
		if(path == null || path.isEmpty())
			return true;
		else 
			return false;
	}
	
	public LinkedList<Contig> getPath()
	{
		return path;
	}

	@Override
	public String toString() {
		return "InternalPath [path=" + path + ", score=" + score + "]";
	}
}


