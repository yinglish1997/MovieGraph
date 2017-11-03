# MovieGraph
storing movie into a graph using neo4j

MovieGraph.java:定义了图谱的基本结构</br></br>
CreateGraph.java: 图谱的构造方法，构造函数参数为TXT文件和希望生成的数据库所在位置</br></br>
ReadGraph.java: 读取图谱信息。</br></br>
searchGraph.java: 用以自动问答。需要特别注意的是问题列表。</br></br>
可以参考该类中的main函数中示例。所有的问题必须按照图谱的结构来。</br></br>
譬如想询问一个角色是谁演的，“电影名，角色列表，角色，确切的一个角色名，演员名字“</br></br>
询问演员的信息：”电影名，角色列表，角色，确切角色名，演员，出生日期/国籍/过去作品……“</br></br>
所以这个图谱信息一定要记住：</br></br>
<img src="graph.png">
