package graph;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

//import Graph.MovieGraph.Labels;
//import Graph.MovieGraph.MyRelationshipTypes;


public class MovieGraph {
	/*
	 * 
	 * 构建底层的neo4j图谱
	 * 
	 */
	
	//在角色那里添加国籍，在内容那里添加豆瓣链接
	public GraphDatabaseService graphDB ;
	//private static GraphDatabaseService graphDB =new GraphDatabaseFactory().newEmbeddedDatabase(new File("/tmp/neo4j1"));
	public  Node movie ;
	//一级结点有制作、主题、角色、内容
	public  Node manufacture ;
	public  Node theme ;
	public  Node roleList ;
	public  Node contence ;
	//二三级结点在构造类中动态创建，不在这里作为类变量
		
	public MovieGraph(String path) throws IOException {
		//在指定位置创建数据库
		System.out.println("dataBasePath:  " +  path);	
		File file = new File(path);
		try{
			if(!file.exists()){//如果数据库文件不存在，则直接新建数据库
			//file.createNewFile();
				init(false, path);
			}
//			else{
//				init(true, path);//如果数据库已经存在，要删除之前的
//			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
	public void init(boolean deletOrNot, String path) throws IOException{
		//创建数据库，并初始化结点，初始化中心结点与一级结点间的关系
		if(deletOrNot){//如果数据库文件已经存在，则删除以前的
			FileUtils.deleteRecursively(new File(path));//关闭寄存器
		}
		 this.graphDB =new GraphDatabaseFactory().newEmbeddedDatabase(new File(path));
		 registerShutdownHook(this.graphDB);
			try(Transaction tx = this.graphDB.beginTx()){
				 this.movie = this.graphDB.createNode();
				 this.manufacture = this.graphDB.createNode();
				 this.theme = this.graphDB.createNode();
				 this.contence = this.graphDB.createNode();
				 this.roleList = this.graphDB.createNode();
						//建立关系：只在movie结点和一级结点建立关系。而后的二三级，由于数目的不确定，都是动态建立
				this.movie.createRelationshipTo(this.manufacture, MyRelationshipTypes.MADED_BY);	
				this.movie.createRelationshipTo(this.theme, MyRelationshipTypes.THEME_IS);		
				this.movie.createRelationshipTo(this.contence, MyRelationshipTypes.CONTENCE_IS);		
				this.movie.createRelationshipTo(this.roleList, MyRelationshipTypes.PLAYED_BY);
				//检索一下中心结点及一级结点的关系，用来检测
//				Iterable<Relationship> allRelationships = this.movie.getRelationships();
//				for(Relationship r: allRelationships){
//					System.out.println("relationName: " + r.getType().name());
//				}
				tx.success();
			}
	}
	private void registerShutdownHook(final GraphDatabaseService graphDB){
		//关闭寄存器
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				graphDB.shutdown();
			}
		});
	}


	//labels
	public enum Labels implements Label{
		MOVIE, MANUFACTURE, THEME, ROLELIST, CONTENCE,
		COMPANY, DIRECTOR, SCRIPTWRITER, ROLE, ACTOR
	}
	//relationships
	public static enum MyRelationshipTypes implements RelationshipType{
		MADED_BY, THEME_IS, CONTENCE_IS, PLAYED_BY,
		COMPANY_IS, DIRECTED_BY, WOROTE_BY, ROLE_IS, ACTOR_BY
	}

	//一级结点的创建都用createxxxNode(properties)
		public  void createMovieNode(String name, String score, String boxOffice, String release_time, String area, 
			String length, String imgurl, String[] keywords){
		try(Transaction tx = graphDB.beginTx()){
			//诸如电影的名称、票房、时长等这些属性一般不会有空值，因此不判断
					//this.movie = this.graphDB.createNode();
					this.movie.setProperty("name", name);
					this.movie.setProperty("score", score);
					this.movie.setProperty("boxOffice", boxOffice);
					this.movie.setProperty("release_time", release_time);
					this.movie.setProperty("area", area);
					this.movie.setProperty("length", length);
					this.movie.setProperty("imgurl", imgurl);
					this.movie.setProperty("keywords", keywords);
					this.movie.addLabel(Labels.MOVIE);
					System.out.println("success to create movie node");					
					tx.success();
		}
	}
		
		
	public  void createManufactureNode(String[] companyList, String[] directorList, String[] scriptwritersList){
		try(Transaction tx = graphDB.beginTx()){			
		   //  manufacture = graphDB.createNode();
			this.manufacture.setProperty("companyList", companyList);
			this.manufacture.setProperty("director", directorList);
			this.manufacture.setProperty("scriptwriters", scriptwritersList);
			this.manufacture.addLabel(Labels.MANUFACTURE);
			System.out.println("success to create manufacture node");
			tx.success();
		}
	}
	
	public  void createContenceNode(String story, String douban, String[] comment, String[] prize){
		try(Transaction tx = graphDB.beginTx()){
				this.contence.setProperty("story", story);
				this.contence.setProperty("douban", douban);
				this.contence.setProperty("comment", comment);
				this.contence.setProperty("prize", prize);
				this.contence.addLabel(Labels.CONTENCE);					
				System.out.println("success to create contence node");
				tx.success();
		}
	}	
		public void createThemeNode(String[] style, String mainTheme, String[] similarMovie){
		try(Transaction tx = graphDB.beginTx()){
					theme.setProperty("style", style);
					theme.setProperty("theme", mainTheme);
					theme.setProperty("similarMovie", similarMovie);
					theme.addLabel(Labels.THEME);					
					System.out.println("success to create theme node");
					tx.success();
		}
	}
	public void createRoleListNode(String[] roleNameList){
		try(Transaction tx = graphDB.beginTx()){
					roleList.setProperty("roleList" , roleNameList);
					roleList.addLabel(Labels.ROLELIST);				
					System.out.println("success to create roleList node");
					tx.success();
		}
	}
	
	//二三级结点的构造方法：返回结点
	public  Node companyNode(String cname, String[] futureRepresent, String[] pastRepresent){
		Node company = null;
		try(Transaction tx = graphDB.beginTx()){   
			 company = graphDB.createNode();
			 company.setProperty("cname", cname);
			 company.setProperty("futureRepresent", futureRepresent);
			 company.setProperty("pastRepresent", pastRepresent);
			 company.addLabel(Labels.COMPANY);		 
			this.manufacture.createRelationshipTo(company, MyRelationshipTypes.COMPANY_IS);
			//System.out.println("company:  " + cname);
			tx.success();
		}
		return company;
	}
	public Node directorNode(String dname, String imgurl, String rate, String[] represent){
		Node director= null;
		try(Transaction tx = graphDB.beginTx()){
			director= graphDB.createNode();	    
			director.setProperty("dname", dname);
			director.setProperty("imgurl", imgurl);
			director.setProperty("rate", rate);
			director.setProperty("represent", represent);
			director.addLabel(Labels.DIRECTOR);
			this.manufacture.createRelationshipTo(director, MyRelationshipTypes.DIRECTED_BY);
			tx.success();
		}
		return director;
	}
	
	public  Node scriptwriterNode(String name, String[] represent){
		Node    scriptwriter = null;
		try(Transaction tx = graphDB.beginTx()){
			scriptwriter = graphDB.createNode();
			scriptwriter.setProperty("name", name);
			scriptwriter.setProperty("represent", represent);
			scriptwriter.addLabel(Labels.SCRIPTWRITER);
			this.manufacture.createRelationshipTo(scriptwriter, MyRelationshipTypes.WOROTE_BY);
			tx.success();
		}
		return scriptwriter;
	}

	public Node roleNode(String name, String imgurl, String introduction, String actor){
		Node role = null;
		try(Transaction tx = graphDB.beginTx()){					
			role = graphDB.createNode();
					role.setProperty("name", name);
					role.setProperty("imgurl", imgurl);
					role.setProperty("introduction", introduction);
					role.setProperty("actor", actor);
					role.addLabel(Labels.ROLE);
					this.roleList.createRelationshipTo(role, MyRelationshipTypes.ROLE_IS);
					tx.success();
		}
		return role;
	}
	
	public  Node actorNode(String name, String nation, String birthday, String imgurl, String pupular, String[] represent){
		Node actor = null;
		try(Transaction tx = graphDB.beginTx()){
			actor = graphDB.createNode();
			actor = graphDB.createNode();
			actor.setProperty("name", name);
			actor.setProperty("nation", nation);
			actor.setProperty("birthday", birthday);
			actor.setProperty("imgurl", imgurl);
			actor.setProperty("pupular", pupular);
			actor.setProperty("represent", represent);
			actor.addLabel(Labels.ACTOR);
			tx.success();
		}
		return actor;
	}

	//用于其他结点关系的动态创建
		public  void buildOtherRel(Node start, Node end, RelationshipType relType){
			try(Transaction tx = graphDB.beginTx()){
				start.createRelationshipTo(end, relType);
				tx.success();
		}
	}

	public static void main(String[] args){

	}
}
