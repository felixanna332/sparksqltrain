package com.data.flink.process.dataset

import org.apache.flink.api.scala.{ExecutionEnvironment, _}

object WCLocal {

  def main(args: Array[String]): Unit = {

    val env = ExecutionEnvironment.getExecutionEnvironment

    val text = env.fromElements(
      "Who's there?",
      "I think I hear them. Stand, ho! Who's there?"
    )
    val counts = text.flatMap {
      _.toLowerCase.split("\\W+").filter {
        _.nonEmpty
      }
    }.map { (_, 1)}
      .groupBy(0)
      .sum(1)

    counts.print()

  }

}
