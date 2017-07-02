

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;









public class CreateGraph {
	
	public static enum MyRelationshipTypes implements RelationshipType{
		MADED_BY, THEME_IS, CONTENCE_IS, PLAYED_BY,
		COMPANY_IS, DIRECTED_BY, WOROTE_BY, ROLE_IS, ACTOR_BY
	}

	/**
	 * 图形的构造方法：在本类中获取所有的属性，并把属性作为参数构造结点，创建关系
	 * 注意构造器中两个路径写的是绝对路径
	 */
	public static FileInputStream in;
	public static InputStreamReader inReader;
	public static BufferedReader bufReader;
	public static String line;
	MovieGraph movieGraph ;
	//以下都是属性字段
	//movie
	String movieName = null;
	String score = null;
	String boxOffice =null;
	String release_time = null;
	String area = null;
	String length = null;
	String imgurl = null;
	String[] keywords = null;
	//contence
	String story = null;
	String douban =null;
	String comment = null;
	String[] commentSplit = null;
	String[] prize = null;
	//theme
	String theme = null;
	String[] style = null;
	String[] similarMovie = null;
	//manufacture
	ArrayList<String> companyList= null;
	List<String> companyName = null;
	List<String>futureRepresent = null;
	List<String>pastRepresent = null;
	//director
	List<String>  dname = null;
	List<String>  directorImgurl = null;
	List<String>  rate = null;
	List<String>  drepresent = null;
	//scriptwriter
	ArrayList<String> scriptArrayList = null;
	List<String>  scriptwriteName = null;
	List<String>  sRepresent = null;
	//role
	ArrayList<String> roleArrayList = null;
    List<String> rname =null;
    List<String> rImg =null;
    List<String> rIntroduction = null;
    //actor
    List<String> actor = null;
    List<String> aBirthday =null;
    List<String> aNation =null;
    List<String> aImg = null;
    List<String> aPupular = null;
    List<String> aRepresent = null;

    
	public String CatchFile(String path, String decode) throws IOException{
		//读取文件：把文件转换为一个字符串返回
		String s = "";
		FileInputStream in = new FileInputStream(path);
		InputStreamReader inReader = new InputStreamReader(in, decode);
		BufferedReader bufReader = new BufferedReader(inReader);
		System.out.println("success to read the txt file");
		while((line = bufReader.readLine()) != null){
			if(!line.equals("--------------------------------------------------------------------"))
				s += line;
		}
		s+= "角色名";//（注：在文件最后添加“角色名”是为了正则容易匹配）
		return s;
	}
    
	public void init(String filePath) throws IOException{
		//通过正则匹配获取属性字段，初始化所有字段
		
		String file = CatchFile(filePath, "UTF-8");//文件路径filePath用于抓取文本，匹配属性
		//movie
		movieName = MatchAndCreate("电影:(.*?)基本介绍", file);
		score = MatchAndCreate("电影评分: (.*?)电影票房", file);
		boxOffice =MatchAndCreate("电影票房: (.*?)上映时间", file);
		release_time = MatchAndCreate("上映时间: (.*?)国家地区", file);
		area = MatchAndCreate("国家地区:  (.*?)片长: ", file);
		length = MatchAndCreate("片长: (.*?)图片: ", file);
		imgurl = changeEmpty(MatchAndCreate("图片:(.*?)关键词列表:", file)); //关于数据空值的处理，单一值的属性用changeEmpty方法，但该方法并不适应于所有字段
		keywords = MatchAndCreateArray("关键词列表: (.*?)内容", file, ";");//MatchAndCreateArray method carry the code to check the empty
		
		//contence
		story = changeEmpty(MatchAndCreate("剧情解析:(.*?)豆瓣评论url:", file));
		douban = changeEmpty(MatchAndCreate("豆瓣评论url:(.*?)影评评价:", file));
	    commentSplit = MatchAndCreate("影评评价:(.*?)获奖情况: ", file).replaceAll("（.*?）", "").split(";"); 
	if(commentSplit.length == 1){
		if(commentSplit[0].length() < 2){
			commentSplit = new String[]{"暂无"};
		}
	}else{
			//过滤掉评论中无意义字符如标点符号
			List<String> tempCom = new ArrayList<String>();
			for(String com: commentSplit){		
				if(com.length() >= 5){//假定长度小于５的为无意义字符
					tempCom.add(com);
		}
				commentSplit = (String[]) tempCom.toArray(new String[tempCom.size()]);
	}	
	}
		prize =  MatchAndCreateArray("获奖情况: (.*?)主题:", file," "); 
		
		//	//theme
		style =  MatchAndCreateArray("风格:(.*?)题材内容:", file," ");
		theme = changeEmpty(MatchAndCreate("题材内容:(.*?)相关电影:", file));
		similarMovie = MatchAndCreateArray("相关电影:(.*?)制作", file, " ");
		
	//manufacture
		companyName = MatchAndCreateList("出品公司名称:(.*?)未来作品:", file);
		futureRepresent = MatchAndCreateList("未来作品:(.*?)过去作品:", file);
		pastRepresent = MatchAndCreateList("过去作品:(.*?)(出品公司名称|导演)", file);
		
		dname = MatchAndCreateList("导演名称:(.*?)图片:", file);
		directorImgurl = MatchAndCreateList("图片:(.*?)评分:", file);
		rate = MatchAndCreateList("评分:(.*?)导演代表作品:", file);
	   drepresent = MatchAndCreateList("导演代表作品:(.*?)(导演名称:|编剧)", file);
		
		scriptwriteName = MatchAndCreateList("编剧名称:(.*?)编剧代表作品:", file);
		sRepresent = MatchAndCreateList("编剧代表作品:(.*?)(编剧名称|角色)", file);

	    rname = MatchAndCreateList("角色名:(.*?)角色图片:", file);
	    rImg =MatchAndCreateList("角色图片:(.*?)角色介绍:", file);
	    rIntroduction = MatchAndCreateList("角色介绍:(.*?)对应的演员:", file);
	    actor = MatchAndCreateList("对应的演员:(.*?)演员出生日期:", file);
	    aBirthday = MatchAndCreateList("演员出生日期:(.*?)演员国籍:", file);
	    aNation =MatchAndCreateList("演员国籍:(.*?)演员图片:", file);
	    aImg = MatchAndCreateList("演员图片:(.*?)演员的受欢迎程度:", file);
	    aPupular = MatchAndCreateList("演员的受欢迎程度:(.*?)演员代表作品:", file);
	    aRepresent = MatchAndCreateList("演员代表作品:(.*?)(角色名)", file);
	}

	public CreateGraph(String dbPath, String filePath) throws IOException{
		File file = new File(dbPath);
		try{
//			if(!file.exists())
//				file.createNewFile();
				//dbPath：数据库位置　filePath: txt文件的位置
				this.movieGraph = new MovieGraph(dbPath);//数据库路径dbPath用于创建数据库
				//init()函数初始化所有字段
				init(filePath);
				
			    //首先创建一级结点的关系
				this.movieGraph.createMovieNode(movieName, score, boxOffice, release_time, area, length, imgurl, keywords);

				this.movieGraph.createContenceNode(story, douban, commentSplit, prize);

				this.movieGraph.createThemeNode(style, theme, similarMovie);
				//一级结点　“制作”　的属性都是数组，所以要把类变量转换成数组先
				String[] companyList = changeEmptyArray((String[]) companyName.toArray(new String[companyName.size()]));
				String[] directorList = changeEmptyArray((String[]) dname.toArray(new String[dname.size()]));
				String[] scriptwritersList =changeEmptyArray( (String[]) scriptwriteName.toArray(new String[scriptwriteName.size()]));
				this.movieGraph.createManufactureNode(companyList, directorList, scriptwritersList);
		        
		        //一级结点　“角色”　的属性也是数组
		        String[] roleList =changeEmptyArray( (String[]) rname.toArray(new String[rname.size()]));
		        this.movieGraph.createRoleListNode(roleList);
				
				//二级结点由于数目的不确定需要在循环中动态创建:　首先都要把动态数组转换为可以作为属性的一般数组，
				//然后多个属性同步下标作为创建函数的参数
				//company
				String[] cnameArray = changeEmptyArray((String[]) companyName.toArray(new String[companyName.size()]));//changeEmptyArray把数组中为空的每一项变为“暂无”，不删除，否则会出现对应关系的错误
				String[] cfutureRepresentArray =changeEmptyArray( (String[]) futureRepresent.toArray(new String[futureRepresent.size()]));
				String[] cpastRepresentArray= changeEmptyArray((String[]) pastRepresent.toArray(new String[pastRepresent.size()]));
				if((companyName.size() == futureRepresent.size()) && (futureRepresent.size() == pastRepresent.size())){
					for(int i = 0; i < cnameArray.length; i ++){				
						Node company= this.movieGraph.companyNode(cnameArray[i],  cfutureRepresentArray[i].split(" "),cpastRepresentArray[i].split(" "));
						//System.out.println("company  " + i + ":  "+ cnameArray[i]);
						//movie.buildOtherRel(movie.manufacture, company, MyRelationshipTypes.COMPANY_IS);
						//System.out.println("build a relation from manufacture to a company" + i);
					}
				}else{
					System.out.println("wrong in company");
				}
			
				//director
				String[] dnameArray =changeEmptyArray( (String[]) dname.toArray(new String[dname.size()]));
				String[] dimgArray = changeEmptyArray((String[]) directorImgurl.toArray(new String[directorImgurl.size()]));
				String[] drateArray =changeEmptyArray( (String[]) rate.toArray(new String[rate.size()]));
				String[] drepresentArray= changeEmptyArray((String[]) drepresent.toArray(new String[drepresent.size()]));
				if((dname.size() == directorImgurl.size()) && (directorImgurl.size() == drepresent.size())){
					for(int i = 0; i < dnameArray.length; i ++){				
						Node director = this.movieGraph.directorNode(dnameArray[i], dimgArray[i], drateArray[i], drepresentArray[i].split(" "));
						//System.out.println("director  " + i + ":  " + dnameArray[i]);
						//movie.buildOtherRel(movie.manufacture, director, MyRelationshipTypes.DIRECTED_BY);
						//System.out.println("build a relation from manufacture to a director " + i);
					}
				}else{
					System.out.println("wrong in director");
				}
				//System.out.println("\n");

				//scriptWriter
				String[] snameArray = changeEmptyArray((String[]) scriptwriteName.toArray(new String[scriptwriteName.size()]));
				String[] sRepresentArray=changeEmptyArray( (String[]) sRepresent.toArray(new String[sRepresent.size()]));
				if((scriptwriteName.size() == sRepresent.size())){
					for(int i = 0; i < snameArray.length; i ++){				
						Node scriptwriter = this.movieGraph.scriptwriterNode(snameArray[i], sRepresentArray[i].split(" "));
						//System.out.println("scriptWriter " + i + ":  " + snameArray[i]);
						//movie.buildOtherRel(movie.manufacture, scriptWriter, MyRelationshipTypes.WOROTE_BY);
						//System.out.println("build a relation from manufacture to a scriptWriter " + i);
					}
				}else{
					System.out.println("wrong in scriptwriter");
				}

				//role
				String[] rnameArray = changeEmptyArray((String[])rname.toArray(new String[rname.size()]));
				String[] rimgArray = changeEmptyArray((String[]) rImg.toArray(new String[rImg.size()]));
				String[] rIntroductionArray =changeEmptyArray( (String[]) rIntroduction.toArray(new String[rIntroduction.size()]));
				String[] rActorArray= changeEmptyArray((String[]) actor.toArray(new String[actor.size()]));
				//actor
				String[] aBirthdayArray = changeEmptyArray((String[])aBirthday.toArray(new String[aBirthday.size()]));
				String[] aimgArray =changeEmptyArray( (String[])aImg.toArray(new String[aImg.size()]));
				String[] aNationArray = changeEmptyArray((String[]) aNation.toArray(new String[aNation.size()]));
				String[] aPupularArray =changeEmptyArray( (String[]) aPupular.toArray(new String[aPupular.size()]));
				String[] aRepresentArray= changeEmptyArray((String[]) aRepresent.toArray(new String[aRepresent.size()]));
			   
				if((rname.size() == actor.size())){
					for(int i = 0; (i < rnameArray.length ) && (i < rActorArray.length); i ++){				
						Node actorNode = this.movieGraph.actorNode(rActorArray[i], aNationArray[i], aBirthdayArray[i], aimgArray[i], aPupularArray[i], aRepresentArray[i].split(" "));				
						Node role = this.movieGraph.roleNode(rnameArray[i], rimgArray[i], rIntroductionArray[i],rActorArray[i]);
						//System.out.println(" role  " + i + ":  " + rnameArray[i]);				
						//movie.buildOtherRel(movie.roleList, role, MyRelationshipTypes.ROLE_IS);
						//System.out.println("build a relation from roleList to a role " + i);
					    
						this.movieGraph.buildOtherRel(role, actorNode, MyRelationshipTypes.ACTOR_BY);
					    //System.out.println("create relationship from the above role node to actor");
					}
				}else{
					System.out.println("wrong in role");
				}
				this.movieGraph.graphDB.shutdown();//关掉数据库避免报错memoryError
			//}
		}catch(Exception e){
			e.printStackTrace();
		}

}

	
	public String changeEmpty(String sentence){
		//填补空的值唯一的属性，比如评分，简介这些字段
		if(sentence.length() == 1 || sentence == null){
			sentence = "暂无";
		}
		return sentence;
	}
	public String[] changeEmptyArray(String[] array){
	// 数组中如果有空元素，填补它而不能删除 
		for(int i = 0; i < array.length; i ++){
			if(array[i].length() < 2 || array[i] == null){
				array[i] = "暂无";
			}
		}
		return array;
	}
	public static String MatchAndCreate(String pattern, String file){
	//匹配单一结果（即只有一个属性，像电影名字、时长等等）
		String s = "";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(file);
		if(m.find()){
				s = m.group(1);
		}
		return s;
	}	
	
	public String[] MatchAndCreateArray(String pattern, String file, String split){
		//匹配单一结果返回数组（用于关键字，主题、相关电影）
		String[] s = null;
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(file);
		if(m.find()){
				s = m.group(1).split(split);
				if(s.length == 1){
					if(s[0].length() < 2){
						s = new String[]{"暂无"};
					}
				}
		}
		return s;
	}

	public List<String> MatchAndCreateList(String patternStr, String content){
		//匹配多个结果返回List（导演、编剧、角色会多次循环出现，用链表承载）
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(content);
		List<String> resultList = new ArrayList<String>();
		while(matcher.find()){
			String tmp = matcher.group(1);
			resultList.add(tmp);
		}
		return resultList;
	}


	public static void createAllGraphs(String[] nameList) throws IOException{
		//类构造器
		String txtForthPath = "/home/yingying/下载/"; //文件路径前缀
		String dbForthPath = "/home/yingying/下载/neo4jMovieGraph/";//对应数据库路径前缀
		
		for(int i = 0 ; i < nameList.length; i ++){
			String txtPath =txtForthPath + nameList[i]+ "/" + nameList[i]+ ".txt" ;// 该部电影对应的真正的文件路径
			String dbPath = dbForthPath +nameList[i];//该部电影对应的真正的数据库路径
			CreateGraph test = new CreateGraph(dbPath, txtPath);	
		}
	}
	
	public static void main(String[] args) throws IOException{
		System.out.println("ok");
//		File file = new File("/home/yingying/下载/挑战杯/MovieList");
//		File[] fileList = file.listFiles();
//		int x = 0;
//		for(; x <fileList.length; x ++){
//			String txtPath = fileList[x].getAbsolutePath();			
//			String[] getName = txtPath.split("/");
//			String dbPath = "/home/yingying/下载/neo4jMovieGraph/" + getName[getName.length - 1];
//			//System.out.println(txtPath + "      " + dbPath);
//			CreateGraph graph = new CreateGraph(dbPath, txtPath);
//		}
		CreateGraph graph = new CreateGraph("/home/yingying/桌面/movie/fiveMovieNeo4j/银河护卫队2", "/home/yingying/桌面/movie/5部电影txt/银河护卫队2.txt");
	}
}



