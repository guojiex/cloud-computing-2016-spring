import org.apache.spark._
import SparkContext._

object PageRank {
    def main(args: Array[String]){
        val sc = new SparkContext()

            val textFile = sc.textFile("/input/TwitterGraph.txt")
            var followerList = textFile.map(line => {
                    val parts = line.split("\t")
                    (parts(0), parts(1))
                    }).distinct().groupByKey()
        /* followerList.collect()
res1: Array[(String, Iterable[String])] = Array((10586974,CompactBuffer(30721081, 32670574)), (10792966,CompactBuffer(23132976, 24932217, 21570687, 21514441))
         */
            val followees=followerList.keys

            val vertices=textFile.flatMap(line => line.split("\t")).distinct()
            val verticesCount=vertices.count()

            val danglingUserList=vertices.subtract(followees).collect()

            val danglingUser=for(i<-danglingUserList) yield(i, List())

            //make up the edge set, those without followees have empty list
            val edgeSet=followerList++sc.parallelize(danglingUser)
            var pageRank=vertices.map(userId=>(userId,1.0))
            for( i <- 1 to 10) {
                println("============Iteration:%d start".format(i))
                val danglingSum = sc.accumulator(0.0)
                val connect=edgeSet.join(pageRank).flatMap{
                    case(userId,(follows,rank))=>{
                        if(follows.isEmpty){
                            danglingSum+=rank
                            List()
                        }else{
                            follows.map(follower=>(follower,rank/follows.size))
                        }
                    }
                }
                connect.count()
                val tempSum=danglingSum.value
                pageRank=connect.reduceByKey(_ + _).mapValues(rank=> 0.15 + 0.85 * (rank + tempSum / verticesCount))
            }
        pageRank.map(t=> "%s\t%s".format(t._1,t._2)).cache().saveAsTextFile("/pagerank-output")
    }
}

