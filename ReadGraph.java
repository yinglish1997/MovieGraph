package graph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import graph.MovieGraph;
import graph.ReadGraph;
import graph.MovieGraph.Labels;
import graph.ReadGraph.Actor;
import graph.ReadGraph.Company;
import graph.ReadGraph.Contence;
import graph.ReadGraph.Director;
import graph.ReadGraph.Introduction;
import graph.ReadGraph.MainTheme;
import graph.ReadGraph.Manufacture;
import graph.ReadGraph.Role;
import graph.ReadGraph.Scriptwriter;

public class ReadGraph {

	/**
	 * 读取图的类，包含introduction\contence\mainTheme\maker\role四大类
	 * 首先要跑通CreateGraph类，使数据库存在后才可以从数据库中读取到相关的信息
	 * Introduction
	 * 		- name, score, boxOffice, length, area, release_time, imgUrl, keywords(String[])
	 *   		 - printIntroduction()
	 *Contence
	 *     	- story, doubanUrl, comment(string[]), prize(string[])
	 *      	-printContence()
	 *MainTheme
	 *     	- theme, style(string[]), similarMovie(string[])
	 *      	- printMainTheme()
	 *Manufacture
	 *     	 - companies(List<Company>), directors(List<Director>), scriptwriters (List<Scriptwriter>)
	 *Company
	 *      	- cname, futureRepresent(string[]), pastRepresent(string[])
	 *      	-printCompany()
	 *Director
	 *     	- dname, directorImgUrl, rate, represent(string[])
	 *      	- printDirector()
	 *Scriptwriter
	 *    		 - sname, sRepresent(string[])
	 *     	 -printScriptwriter()
	 *Role
	 *     	- rname, rImg, rIntroduction, actor(Actor)
	 *    		 -printRole
	 *Actor
	 *    		-actor, birthday, nation, aImg, aPupular, aRepresent(string[])
	 *    		-printActor()
	 *
	 */
	public GraphDatabaseService graphDB;

		Introduction introduction = null;
		Contence contence = null;
		MainTheme mainTheme  = null;
		Manufacture maker = new Manufacture();
		List<Role> role = null;
		Actor tempActor = null;
	
	public ReadGraph(String dbPath){
		this.graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPath));
		this.introduction = readIntroduction();
		this.mainTheme = readMainTheme();
		this.contence = readContence();
		this.maker.companies = readCompanyList();
		this.maker.directors= readDirectorList();
		this.maker.scriptwriters= readScriptwriterList();
		this.role = readRoleList();
		//readRoleList();
	}

	public Node findNodeByLabel(Labels label){ 
		//通过标签查找结点，注意只适用于该标签唯一的情况，比如标签电影，内容，制作，主题等，
		//而像有多个结点拥有公司标签的情况不适用
		Node node= null;
		try(Transaction tx = this.graphDB.beginTx()){
			 	ResourceIterator<Node> iterator = this.graphDB.findNodes(label);			
			 	if(iterator.hasNext()){
			 		node = iterator.next();
			 	}
			 	if(node == null){
			 		System.out.println("wrong to find the node" + label.name());
			 	}
			 	tx.success();
		}
		return node;
	}
	
	public Introduction readIntroduction(){
		Introduction intro ;
		Node movie = findNodeByLabel(Labels.MOVIE);
		try(Transaction tx = this.graphDB.beginTx()){
			 System.out.println(movie.getProperty("name"));
			 String name = (String) movie.getProperty("name");
			 String score = (String) movie.getProperty("score");
			 String boxOffice = (String)movie.getProperty("boxOffice");
			 String release_time= (String) movie.getProperty("release_time");
			 String area = (String)movie.getProperty("area");
			 String length = (String) movie.getProperty("length");
			 String imgurl = (String) movie.getProperty("imgurl");
			 String[] keywords =(String[]) movie.getProperty("keywords");
			intro = new Introduction(name, score, boxOffice, release_time, length, area, imgurl, keywords);
			 //ntro.printIntroduction();
			 tx.success();
		     }
		return intro;		
	}

	public MainTheme  readMainTheme(){
		MainTheme theme ;
		Node themeNode= findNodeByLabel(Labels.THEME);
		try(Transaction tx = this.graphDB.beginTx()){
			 String[] style = (String[]) themeNode.getProperty("style");
			 String themeProperty = (String) themeNode.getProperty("theme");
			 String[] similarMovieProperty = (String[])themeNode.getProperty("similarMovie");
			theme = new MainTheme(style, themeProperty, similarMovieProperty);
			 //ntro.printIntroduction();
			 tx.success();
		     }
		return theme;		
	}
	
	public Contence readContence(){
		//contence == information
		Contence cont;
		Node contence =  findNodeByLabel(Labels.CONTENCE);
		try(Transaction tx = this.graphDB.beginTx()){		
			 String storyProperty = (String) contence.getProperty("story");
			 String[] commentProperty = (String[]) contence.getProperty("comment");
			 String[] prizeProperty = (String[])contence.getProperty("prize");
			 String doubanProperty= (String)contence.getProperty("douban");
			cont = new Contence(storyProperty, doubanProperty, commentProperty, prizeProperty);
			 //ntro.printIntroduction();
			 tx.success();
		     }
		return cont;		
	}
	
	public List<Company> readCompanyList(){
		//注意出品公司，编剧等人的迭代会有重复现象
		List<Company> companyList = new ArrayList<Company>();//最后返回的结果
		ArrayList<String> cnameArrayList = new ArrayList<String>();//存放名字，主要是用ArrayList的contains方法来去重
		String cname = "";//存放属性字段
		String[] cfReArrayList = new String[]{};
		String[] cpReArrayList = new String[]{};
		Company companyNode ;//每次新建一个的company类
		try(Transaction tx = this.graphDB.beginTx()){
			 ResourceIterator<Node> companies = this.graphDB.findNodes(Labels.COMPANY);
			 Node company = null;			
			while(companies.hasNext()){
				 company = companies.next();			 
				 if(company== null){
					 System.out.println("wrong to find the company  node");
				 }
				 String tempcname = (String) company.getProperty("cname");
				 if(!cnameArrayList.contains(tempcname)){//当不重复的时候(发现通过标签取出的结点有重复现象)
					 cnameArrayList.add((String) company.getProperty("cname"));
					 cname = (String) company.getProperty("cname");
					 cfReArrayList = (String[]) company.getProperty("futureRepresent");
					 cpReArrayList= (String[]) company.getProperty("pastRepresent");
					 companyNode = new Company(cname,  cfReArrayList, cpReArrayList);
					 companyList.add(companyNode);
				 }
			}
//			for(Company cm: companyList){
//				cm.printCompany();
//			}
			 tx.success();
		     }
		return companyList;		
	}
	
	public List<Director> readDirectorList(){
		//注意出品公司，编剧等人的迭代会有重复现象
		List<Director> directorList = new ArrayList<Director>();//最后返回的结果
		ArrayList<String> dnameArrayList = new ArrayList<String>();//存放名字，主要是用ArrayList的contains方法来去重
		String dname = "";//存放属性字段
		String directorImg = "";
		String rate = "";
		String[] ReArrayList = new String[]{};
		Director  directorNode ;//每次新建一个的company类
		try(Transaction tx = this.graphDB.beginTx()){
			 ResourceIterator<Node> directors = this.graphDB.findNodes(Labels.DIRECTOR);
			 Node director = null;			
			while(directors.hasNext()){
				director = directors.next();			 
				 if(director== null){
					 System.out.println("wrong to find the director  node");
				 }
				 String tempdname = (String) director.getProperty("dname");
				 if(!dnameArrayList.contains(tempdname)){//当不重复的时候
					 dnameArrayList.add((String) director.getProperty("dname"));
					 dname = (String) director.getProperty("dname");
					 directorImg = (String) director.getProperty("imgurl");
					 rate = (String) director.getProperty("rate");
					 ReArrayList= (String[]) director.getProperty("represent");
					 directorNode = new Director(dname, directorImg, rate, ReArrayList);
					 directorList.add(directorNode);
				 }
			}
			 tx.success();
		     }
		return directorList;		
	}
	public List<Scriptwriter> readScriptwriterList(){
		//注意出品公司，编剧等人的迭代会有重复现象
		List<Scriptwriter> ScriptwriterList = new ArrayList<Scriptwriter>();//最后返回的结果
		ArrayList<String> snameArrayList = new ArrayList<String>();//存放名字，主要是用ArrayList的contains方法来去重
		String sname = "";//存放属性字段
		String[] sRepresent = new String[]{};

		Scriptwriter  ScriptwriterNode ;//每次新建一个的company类
		try(Transaction tx = this.graphDB.beginTx()){
			 ResourceIterator<Node>  Scriptwriters = this.graphDB.findNodes(Labels.SCRIPTWRITER);
			 Node Scriptwriter = null;			
			while(Scriptwriters.hasNext()){
				Scriptwriter = Scriptwriters.next();			 
				 if(Scriptwriter== null){
					 System.out.println("wrong to find the Scriptwriter  node");
				 }
				 String tempsname = (String) Scriptwriter.getProperty("name");
				 if(!snameArrayList.contains(tempsname)){//当不重复的时候
					snameArrayList.add((String) Scriptwriter.getProperty("name"));
					 sname = (String) Scriptwriter.getProperty("name");
					 sRepresent= (String[]) Scriptwriter.getProperty("represent");
					 ScriptwriterNode  =  new Scriptwriter(sname, sRepresent);
					 ScriptwriterList.add(ScriptwriterNode);
				 }
			}
			 tx.success();
		     }
		return ScriptwriterList;		
	}

	public List<Role> readRoleList(){
		//注意出品公司，编剧等人的迭代会有重复现象
		List<Role> RoleList = new ArrayList<Role>();//最后返回的结果
		ArrayList<String> rnameArrayList = new ArrayList<String>();//存放名字，主要是用ArrayList的contains方法来去重
		String rname = "";//存放属性字段
		String imgUrl = "";
		String introduction = "";
		
		try(Transaction tx = this.graphDB.beginTx()){//首先查找标签为ROLE的结点
			 ResourceIterator<Node>  Roles = this.graphDB.findNodes(Labels.ROLE);
			 Node Role = null;			
			while(Roles.hasNext()){
				Role= Roles.next();			 
				 if(Role== null){
					 System.out.println("wrong to find the Role  node");
				 }
				 String temprname = (String) Role.getProperty("name");
				 if(!rnameArrayList.contains(temprname)){//当不重复的时候
					rnameArrayList.add((String) Role.getProperty("name"));
					 rname = (String) Role.getProperty("name");
					 imgUrl= (String) Role.getProperty("imgurl");
					 introduction = (String) Role.getProperty("introduction");
					// System.out.println("roleName: " + rname);//接着通过角色与演员的关系ACTOR_BY来找到对应的演员
					 for(Relationship r1: Role.getRelationships(MovieGraph.MyRelationshipTypes.ACTOR_BY)){
						 Node actorNode = r1.getOtherNode(Role);//获得演员结点
						 String aName = (String) actorNode.getProperty("name");//获取演员的属性
						 //System.out.println("the relative actor is : " + aName);
						 String nation = (String) actorNode.getProperty("nation");
						 String birthday = (String) actorNode.getProperty("birthday");
						 String imgurl= (String) actorNode.getProperty("imgurl");
						 String pupular = (String) actorNode.getProperty("pupular");
						 String[] represent = (String[]) actorNode.getProperty("represent");
						 Actor tempActor = new Actor(aName, birthday, nation, imgurl, pupular, represent);//创建演员类实体
						 Role RoleNode= new Role(rname, imgUrl, introduction, tempActor);//完成一个角色类实体的创建
						 RoleList.add(RoleNode);//最后返回所有的角色
					 }
					 //System.out.println("------------------------------------------------------------------------------------------------------");
				 }
			}
			 tx.success();
		     }
		return RoleList;		
	}
	
	public Node returnRelativeActor(Node role){
		Node actor  = null;
		try(Transaction tx = this.graphDB.beginTx()){
			Iterable<Relationship> allRelationships = role.getRelationships();
			for(Relationship r: allRelationships){
				System.out.println(r.toString());
				if(r.getType().name().equalsIgnoreCase("ACTOR_BY")){
					System.out.println("success to find the relative actor");
					actor = r.getEndNode();
				}
			}		
		}
		return actor;
	}
	
	class Introduction{  
		String name; 
		String score ;	//豆瓣评分
		String boxOffice ;	 //票房成绩
		String length; 	//片长
		String area; 		//国家地区
		String release_time; 	//上映日期
		String imgUrl;
		String[] keywords;  //关键词列表
		//构造函数:
		public Introduction(){
			name = "";
			 score  = "";	//豆瓣评分
			 boxOffice = "" ;	 //票房成绩
			 length = ""; 	//片长
			 area = ""; 		//国家地区
			 release_time = ""; 	//上映日期
			 imgUrl = "";
			 keywords = new String[]{"暂无"};  //关键词列表
			 }
		public Introduction(String name, String score, String boxOffice,  String release_time, String length, String area, String imgUrl, String[] keywords){
			this.name = name;
			this.score = score;
			this.boxOffice = boxOffice;
			this.release_time = release_time;
			this.length = length;
			this.area= area;
			this.imgUrl = imgUrl;
			this.keywords = keywords;
		}
	public void printIntroduction(){
		System.out.println("\n" + "------------------------------------introduction--------------------------------------");
		System.out.println(name);
		System.out.println(score);
		System.out.println(boxOffice);
		System.out.println(release_time);
		System.out.println(length);
		System.out.println(area);
		System.out.println(imgUrl );
		for(String word: keywords){
				System.out.print(word);
		}
		System.out.print("\n");
	}		
}
	
	
	class Contence{
		String story; //剧情解析
		String[]  comment;//影评评价
		String[]  prize; //获奖记录
		String doubanUrl;
		public Contence(){
			story = "";
		}
		public Contence(String story, String douban, String[] comment, String[] prize){
			this.story = story;
			this.comment = comment;
			this.doubanUrl = douban;
			this.prize = prize;
		}
		public void printContence(){
			System.out.println("------------------------------------information--------------------------------------");
			System.out.println("＃剧情解析＃");
			System.out.println(story);
			System.out.println("＃豆瓣评论＃");
			System.out.println(doubanUrl);
			System.out.println("＃影评评价＃");
			for(String word: comment){
				System.out.println(word);
			}
			System.out.println("＃获奖情况＃");
			for(String onePrize: prize){
				System.out.println(onePrize + "    ");
			}
		}	
	}
	
	class MainTheme{
		String[] style;
		String theme;
		String[] similarMovie;
		public MainTheme(String[] style, String theme, String[] similarMovie){
			this.style = style;
			this.theme = theme;
			this.similarMovie = similarMovie;
		}
		public void printMainTheme(){
			System.out.println("------------------------------------mainTheme--------------------------------------");
			System.out.println("＃题材内容＃");
			System.out.println(theme);
			System.out.println("＃风格＃");
			for(String word: style){
				System.out.print(word);
			}
			System.out.print("\n");
			System.out.println("＃相关电影＃");
			for(String oneMovie: similarMovie){
				System.out.print(oneMovie + "   ");
			}
			System.out.print("\n");
		}	
	}
	
	class Manufacture{
		List<Company> companies ;
		List<Director> directors ;
		List<Scriptwriter>  scriptwriters ;
		
		public void  printManufacture(){
			System.out.println("\n");
			System.out.println("------------------------------------manufacture--------------------------------------");
			for(Company oneCompany: companies){
				oneCompany.printCompany();
			}					
			System.out.println("\n");
			System.out.println("------------------------------------director");
			for(Director oneDirector: directors){
				 oneDirector.printDirector();
			}			
			System.out.println("\n");
			System.out.println("------------------------------------scriptwriter");
			for(Scriptwriter sw: scriptwriters){
				sw.printScriptwriter();
			}
		}	
	}
	
 class Company{
	 String cname ="";
	 String[] futureRepresent = null;
	 String[] pastRepresent= null;
	 public Company() {
			
	}
	 public Company(String name, String[] fRepresent, String[] pRepresent){
		 this.cname= name;
		 this.futureRepresent = fRepresent;
		 this.pastRepresent =  pRepresent;
	 }

	public void printCompany(){
			System.out.println("companyName:  " + this.cname);
			System.out.println("company 's future represent:    ") ;
			for(String fRep: this.futureRepresent){
				System.out.print(fRep + "   ");
				//System.out.print(fRep.length());
			}
			System.out.println("\n" + "compaany's past  represent:    " ) ;
			for(String pRep: this.pastRepresent)
				System.out.print(pRep + "   ");
			System.out.println("\n");
	 }
 }
 
 
 class Director{
	 String dname;
	 String directorImgUrl ;
	 String rate;
	 String[] represent;
	 public Director(String dname, String directorImgUrl, String rate, String[] represent){
		 this.dname = dname;
		 this.directorImgUrl = directorImgUrl;
		 this.rate = rate;
		 this.represent = represent;
	 }
	 public void printDirector(){
			System.out.println("directorName: " + this.dname );
			System.out.println("＃导演图片＃");
			System.out.println(this.directorImgUrl);
			System.out.println("＃导演评分＃");
			System.out.println(this.rate);
			System.out.println("＃代表作＃");
			for(String rep: this.represent){
				System.out.print(rep + "   ");
			}
			System.out.println("\n");
	 }
 }
 
 
 class Scriptwriter{
	 String sname;
	 String[] sRepresent;
	 public Scriptwriter(){
		 sname = "";
		 sRepresent = null;
	 }
	 public Scriptwriter(String name, String[] sRepresent){
		 this.sname = name;
		 this.sRepresent= sRepresent;
	 }
	 public void printScriptwriter(){
			System.out.println("scriptwriterName:  " + this.sname);			
			System.out.println("＃编剧代表作＃");
			for(String sRep: this.sRepresent ){
				System.out.print(sRep+ "   ");
			}
			System.out.print("\n");
	 }
 }
	 
	 
 class Role{
	 String rname;
	 String rImg;
	 String rIntroduction;
	 Actor actor;
	 public Role(String rname, String rImg, String rIntroduction, Actor actor){
		 this.rname = rname;
		 this.rIntroduction = rIntroduction;
		 this.rImg = rImg;
		 this.actor = actor;
	 }
	 public void printRole(){			
		 System.out.println("roleName:  " + this.rname);
		 System.out.println("＃角色图片＃");
		 System.out.println(this.rImg);
			System.out.println("＃角色介绍＃");
		 System.out.println(this.rIntroduction);
		 this.actor.printActor();
	 }
 }
 
 
 class Actor{
	 String actor;
	 String birthday;
	 String nation;
	 String aImg;
	 String aPupular;
	 String[] aRepresent;
	 public Actor(){
		 this.actor = "";
		 this.birthday = "";
		 this.nation = "";
		 this.aPupular = "";
		 this.aImg = "";
		 this.aRepresent = new String[]{""};
	 }
	 public Actor(String actor, String birthday, String nation, String aImg, String aPupular, String[] aRepresent){
		 this.actor = actor;
		 this.birthday = birthday;
		 this.aImg = aImg;
		 this.nation = nation;
		 this.aPupular = aPupular;
		 this.aRepresent = aRepresent;
	 }
	public void printActor() {
		 System.out.println("actorName:  " + this.actor);
		System.out.println("＃演员生日＃");
		 System.out.println(this.birthday);
		 System.out.println("＃演员国籍＃");
		 System.out.println(this.nation);
		 System.out.println("＃演员图片＃");
		 System.out.println(this.aImg);
		 System.out.println("＃演员的受欢迎程度＃");
		 System.out.println(this.aPupular);
		 System.out.println("＃演员代表作品＃");
		 for(String aRep: this.aRepresent){
			 System.out.print(aRep + "   ");
		 }
		 System.out.print("\n");
	}
 }
 
	public static void main(String[] args){
		ReadGraph test = new ReadGraph("/home/yingying/下载/movieDB/一路向前");
		for(Role rol: test.role){
			rol.printRole();
		}
//		for(Company com: test.maker.companies){
//			com.printCompany();
//		}
	}
}
