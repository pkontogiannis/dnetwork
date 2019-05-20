package com.pkon.service.dnetwork

sealed abstract class EndpointMode(val mode: String)

object EndpointMode {

  case object TaskSimple extends EndpointMode(mode = "simple")

  case object TaskBatch extends EndpointMode(mode = "batch")

  case object CompactView extends EndpointMode(mode = "compact")

  case object NormalView extends EndpointMode(mode = "normal")

  def parse(mode: String): Option[EndpointMode] = {
    mode match {
      case TaskSimple.mode => Some(TaskSimple)
      case TaskBatch.mode => Some(TaskBatch)
      case CompactView.mode => Some(CompactView)
      case NormalView.mode => Some(NormalView)
      case _ => None
    }
  }


}
