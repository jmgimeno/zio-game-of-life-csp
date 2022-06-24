package zgameoflife

import zio.*

abstract class Cell:
  val refAlive: Ref[Boolean]
  val tickChannel: Queue[Unit]
  val resultChannel: Queue[Boolean]
  val inChannels: List[Queue[Boolean]]
  val outChannels: List[Queue[Boolean]]

  val run: Task[Unit] =
    for
      alive <- refAlive.get
      _ <- tickChannel.take
      _ <- ZIO.foreachDiscard(outChannels)(ch => ch.offer(alive))
      neighbours <- ZIO.foreach(inChannels)(ch => ch.take)
      nextAlive = nextState(alive, neighbours)
      _ <- refAlive.set(nextAlive)
      _ <- resultChannel.offer(nextAlive)
    yield ()

  val start: Task[Nothing] =
    run.forever
    
  private def nextState(alive: Boolean, neighbours: List[Boolean]): Boolean =
    val countNeighbours = neighbours.count(identity)
    if alive then countNeighbours == 2 || countNeighbours == 3
    else countNeighbours == 0
    
object Cell:
  def make(options: CellOptions): Task[Cell] =
    for
      ref <- Ref.make[Boolean](options.alive)
      inCh <- options.inChannels.get
      outCh <- options.outChannels.get
    yield new:
      override val refAlive = ref
      override val tickChannel = options.tickChannel
      override val resultChannel = options.resultChannel
      override val inChannels = inCh
      override val outChannels = outCh
