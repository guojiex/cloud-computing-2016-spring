import org.apache.spark._
import SparkContext._

object Follower {
    def main(args: Array[String]){
      val sc = new SparkContext()
      val textFile = sc.textFile("/input/TwitterGraph.txt").cache()
      val followers=textFile.map(line=>line.split("\t")(1)).map(followeeId=>(followeeId,1)).reduceByKey(_ + _).cache()
      //same as wordcount
      followers.map(tuple2=>"%s\t%s".format(tuple2._1,tuple2._2)).cache().saveAsTextFile("/follower-output")
    }
}
