package com.github.jeroenr.tepkin

import akka.actor.{ActorSystem, PoisonPill}
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.github.jeroenr.tepkin.TepkinMessage.{Idle, Init, ShutDown, _}
import com.typesafe.config.Config

class TepkinMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
  PriorityGenerator {

    case Init => 0

    case Idle | CursorOpened(_) | CursorClosed(_) => 1

    case PoisonPill | ShutDown => 3

    case _ => 2
  }
)
