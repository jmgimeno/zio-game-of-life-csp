package zgameoflife

import zio.*

import gameoflife.PatternParser
import gameoflife.WindowOutput


case class Args(patternFile: String = "patterns/gosper_glider_gun.txt",
                maxWindowWidth: Int = 640,
                maxWindowHeight: Int = 480,
                periodMilliseconds: Int = 20,
                leftPadding: Int = 10,
                topPadding: Int = 10,
                rightPadding: Int = 40,
                bottomPadding: Int = 40,
                rotate: Boolean = false,
                logRate: Boolean = false)


object Main extends ZIOAppDefault :

  val a = Args

  def run = ???
