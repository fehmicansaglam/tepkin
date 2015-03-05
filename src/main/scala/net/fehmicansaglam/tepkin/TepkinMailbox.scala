package net.fehmicansaglam.tepkin

import akka.actor.{ActorSystem, PoisonPill}
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import net.fehmicansaglam.tepkin.TepkinMessages.{Idle, Init, ShutDown}

class TepkinMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
  PriorityGenerator {

    case Init => 0

    case Idle => 1

    case PoisonPill | ShutDown => 3

    case _ => 2
  }
)
