package org.opensplice.mobile.dev.main

import java.util.UUID

import scala.annotation.elidable
import scala.util.Random

import org.opensplice.mobile.dev.common.{ DDSDAbstraction, DDSIdentifier }
import org.opensplice.mobile.dev.main.group.{ EGManager, SGManager }
import org.opensplice.mobile.dev.main.le.EventualLEManager
import org.opensplice.mobile.dev.main.paxos.PaxosManager
import org.opensplice.mobile.dev.tools.DALogger

object Main extends App with DALogger {

  @elidable(1000)
  def elidable() {
    println("Elidable Not enabled")
  }

  override def main(args: Array[String]) {
    elidable()

    // Extracting Options from args 
    var manager: Option[Manager] = None
    val configMap = nextOption(args.toList, Map[String, Any]())

    if (configMap.size == 0)
      return

    // Ouput extacted configuration
    configMap.foreach(x => logger.info(x._1 + "=" + x._2))

    val rand = new Random()
    var actorId = new UUID(0, rand.nextInt(100000))

    if (configMap.contains("aid")) {
      val value = configMap.get("aid").get
      actorId = new UUID(0, value.asInstanceOf[String].toInt)
    }

    var instanceId = "DefaultGroup"

    if (configMap.contains("iid")) {
      instanceId = configMap.get("iid").asInstanceOf[String]
    }

    val identifier = DDSIdentifier(instanceId, actorId)

    if (configMap.contains("eg")) {

      val optionList = configMap.get("eg").get.asInstanceOf[List[String]]
      manager = Some(new EGManager(identifier))
      manager.get.manageArgs(optionList)

    } else if (configMap.contains("ele")) {

      val optionList = configMap.get("ele").get.asInstanceOf[List[String]]

      manager = Some(new EventualLEManager(identifier))
      manager.get.manageArgs(optionList)

    } else if (configMap.contains("p")) {

      val optionList = configMap.get("p").get.asInstanceOf[List[String]]

      manager = Some(new PaxosManager(identifier))
      manager.get.manageArgs(optionList)

    } else if (configMap.contains("sg")) {

      val optionList = configMap.get("sg").get.asInstanceOf[List[String]]
      manager = Some(new SGManager(identifier))
      manager.get.manageArgs(optionList)

    }

    if (manager.isDefined && configMap.contains("i")) {
      println("interactive")
      while (true) {
        val command = Console.readLine()
        val splittedCommand = command.split(" ")
        println(command);
        manager.get.manageCommand(splittedCommand.toList)
      }
    }

  }

  private def nextOption(optionList: List[String], configMap: Map[String, Any]): Map[String, Any] = {
    optionList match {

      case "-aid" :: value :: tail => {
        nextOption(tail, configMap ++ Map("aid" -> value))
      }

      case "-iid" :: value :: tail => {
        nextOption(tail, configMap ++ Map("iid" -> value))
      }

      case "-eg" :: tail => {
        configMap ++ Map("eg" -> tail)
      }

      case "-ele" :: tail => {
        configMap ++ Map("ele" -> tail)
      }

      case "-p" :: tail => {
        configMap ++ Map("p" -> tail)
      }

      case "-sg" :: tail => {
        configMap ++ Map("sg" -> tail)
      }

      case "-esper" :: tail => {
        import org.opensplice.mobile.dev.common.DDSDAbstraction
        DDSDAbstraction.ESPER = true;
        nextOption(tail, configMap)
      }
      
      case "-i" :: tail => {
        nextOption(tail, configMap ++ Map("i" -> true))
      }

      case _ => {
        println("Main Usage:\n" +
          "-i interactive\n" +
          "-esper add esper client" +
          "-aid <value> set the id (Int) of the actor\n" +
          "-aid <value> set the id (String) of the instance\n" +
          "-eg create standard eventual group\n" +
          "-ele create standard eventual leader election\n" +
          "-p delegates to paxos manager\n" +
          "-sg delegates to stable group manager\n")

        configMap
      }
    }

  }

}
