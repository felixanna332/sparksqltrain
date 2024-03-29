package com.data.etl

import org.apache.spark.SparkConf
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.{DataFrame, SparkSession}

trait DataProcessTrait {

  def extractData(sql:String, params:String): DataFrame
  def extractData(path:String): DataFrame

  def loadData(sql:String, params:String)
  def transData(sql:String, params:String)

}

case class clientForAI(id:Long,name:String)

class DataProcess extends  DataProcessTrait {
  private val conf = new SparkConf()
    .setAppName("DataProcess")
    //rdd压缩  只有序列化后的RDD才能使用压缩机制
    .set("spark.rdd.compress", "true")
    //设置并行度
    .set("spark.default.parallelism", "100")
    //使用Kryo序列化库
//    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
//    .set("spark.kryo.registrationRequired", "true")
//    .registerKryoClasses(Array(classOf[scala.collection.mutable.WrappedArray.ofRef[_]], classOf[clientForAI]))
    //优化shuffle 读写
    .set("spark.shuffle.file.buffer","128k")
    .set("spark.reducer.maxSizeInFlight","96M")
    //合并map端输出文件
    .set("spark.shuffle.consolidateFiles", "true")
  //设置executor堆外内存
  //    .set("spark.yarn.executor.memoryOverhead","2048M")
      .setMaster("local")

  val spark = SparkSession
    .builder()
    .config(conf)
    //解决DecimalType存储精度问题， parquet格式 spark和hive不统一
    .config("spark.sql.parquet.writeLegacyFormat", true)
    .config("spark.sql.warehouse.dir", "/user/hive/warehouse/bigdata.db")
    //数据倾斜
    .config("spark.sql.shuffle.partitions", 30)
    .enableHiveSupport()
    .getOrCreate()

  val sc =  spark.sparkContext


  val tblName = "hive_table"

  def dropPartitions(tblName: String,sqlContext :HiveContext): Unit = {

    sqlContext.sql(s"msck repair table $tblName")

  }

  override def extractData(path: String): DataFrame = {
    import spark.implicits._
    val textFile = sc.textFile(path) //"hdfs://user/spark/aa.txt"
    val textFile2 = sc.textFile(path) //"file:///home/spark/aa.txt"
    textFile.first()
    textFile.count()
    textFile.filter(line => line.contains("spark")).count()
    textFile.map(line => line.split(" ").size).reduce((a, b) => if(a > b) a else b)
    val wc = textFile.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey((x, y) => x + y)
    wc.collect()

    //如果想让代码更简洁， 可以使用占位符”_”。
    //  当每个参数在函数文本中最多出现一次的时候， 可以使用下划线_+_扩展成带两个参数的函数文本。
    //  多个下划线指代多个参数， 而不是单个参数的重复使用。 第一个下划线代表第一个参数， 第二个下划线 代表第二个参数。
    val wordC = textFile.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).map(m => (m._1 ,m._2)).sortByKey(false).map(m => (m._2, m._1))
    wordC.saveAsTextFile("hdfs://user/spark/out/wordC")
    wordC.toDF()
  }

  override def loadData(sql: String, params: String): Unit = {

  }

  override def transData(sql: String, params: String): Unit ={

  }


  def extractData(sql:String, params:String): DataFrame = {
    import spark.implicits._
    val textFile = sc.textFile("hdfs://user/spark/aa.txt")
    val textFile2 = sc.textFile("file:///home/spark/aa.txt")
    textFile.first()
    textFile.count()
    textFile.filter(line => line.contains("spark")).count()
    textFile.map(line => line.split(" ").size).reduce((a, b) => if(a > b) a else b)
    val wc = textFile.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey((x, y) => x + y)
    wc.collect()

    //如果想让代码更简洁， 可以使用占位符”_”。
    //  当每个参数在函数文本中最多出现一次的时候， 可以使用下划线_+_扩展成带两个参数的函数文本。
    //  多个下划线指代多个参数， 而不是单个参数的重复使用。 第一个下划线代表第一个参数， 第二个下划线 代表第二个参数。
    val wordC = textFile.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).map(m => (m._1 ,m._2)).sortByKey(false).map(m => (m._2, m._1))
    wordC.saveAsTextFile("hdfs://user/spark/out/wordC")
    wordC.toDF()

  }



  def mapDemo: Unit ={
    var datasRdd = sc.parallelize(Array(1,2,3,4,5,6,7,8), 2)
    var result = datasRdd.map(x => x * 2)
    result.foreach(println(_))
    sc.stop()

  }

  def filterDemo: Unit = {
    var datasRdd = sc.parallelize(Array(1,2,3,4,5,6,7,8))
    var result = datasRdd.filter(x => x % 2 != 0)
    result.foreach(println(_))
    sc.stop()
  }

  def groupByKeyDemo: Unit = {

    var dataRDD = sc.parallelize(Array(Tuple2("class1",90),Tuple2("class1",91),
      Tuple2("class2",91), Tuple2("class2",93)))
    var result = dataRDD.groupByKey()
    result.foreach(it => {
      println(it._1)
      println(it._2.toString())})
    sc.stop()

  }

  def reduceByKeyDemo: Unit = {
    var dataRDD = sc.parallelize(Array(Tuple2("class1",90),Tuple2("class1",91),Tuple2("class2",92),Tuple2("class2",93)))
    var result = dataRDD.reduceByKey(_+_)
    result.foreach(it => {
      println(it._1)
      println(it._2)})
    sc.stop()
  }

  def joinDemo: Unit = {
    val stuRDD = sc.parallelize(Array(Tuple2(1,"zhangsan"),Tuple2(2,"lisi"), Tuple2(3,"liuwe"),Tuple2(4,"")))
  }


}
object DataProcess{

  def main(args: Array[String]): Unit = {

      new DataProcess().reduceByKeyDemo
   }
}
