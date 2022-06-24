package zgameoflife

import zio.*

abstract class GameOfLife:
  val dimensions: Dimensions
  val period: Int
  val gridChannel: Queue[Array[Array[Boolean]]]
  val cells: List[Cell]
  val tickChannels: List[List[Queue[Unit]]]
  val resultChannels: List[List[Queue[Boolean]]]

  val tick: Task[Unit] =
    for
      _ <- dimensions.forEachRowCol { (r, c) =>
        tickChannels(r)(c).offer(()).as(())
      }
      grid = Array.ofDim[Boolean](dimensions.rows, dimensions.cols)
      _ <- dimensions.forEachRowCol { (r, c) =>
        for
          res <- resultChannels(r)(c).take
          _ <- ZIO.succeed { grid(r)(c) = res }
        yield ()
      }
      _ <- gridChannel.offer(grid)
      _ <- ZIO.sleep(period.millis)
    yield ()

  val run: Task[Nothing] =
    tick.forever

  val start: Task[Nothing] =
    ZIO.foreachPar(cells)(_.start).withParallelismUnbounded
      *> run

object GameOfLife:
  def make(
      dims: Dimensions,
      seed: Array[Array[Boolean]],
      per: Int,
      gChannel: Queue[Array[Array[Boolean]]]
    ): Task[GameOfLife] =
    for
      tChannels <- makeGrid[Unit](dims.rows, dims.cols)
      rChannels <- makeGrid[Boolean](dims.rows, dims.cols)
      cellOps <- makeCellOptions(dims.rows, dims.cols, seed, tChannels, rChannels)
      _ <- makeNeighbours(dims, cellOps)
      cs <- makeCells(cellOps)
    yield new GameOfLife:
      override val dimensions = dims
      override val period = per
      override val gridChannel = gChannel
      override val cells = cs
      override val tickChannels = tChannels
      override val resultChannels = rChannels

  private def makeCells(cellOptions: Array[Array[CellOptions]]): Task[List[Cell]] =
    ZIO.foreach(cellOptions.toList) { row =>
      ZIO.foreach(row.toList)(Cell.make)
    }.map(_.flatten)

  private def makeCellOptions(
      rows: Int,
      cols: Int,
      seed: Array[Array[Boolean]],
      tCh: List[List[Queue[Unit]]],
      rCh: List[List[Queue[Boolean]]]
    ): Task[Array[Array[CellOptions]]] =
      ZIO.foreach((0 until rows).toArray) { r =>
        ZIO.foreach((0 until cols).toArray) { c =>
          for
            inCh <- Ref.make[List[Queue[Boolean]]](List.empty)
            outCh <- Ref.make[List[Queue[Boolean]]](List.empty)
          yield CellOptions(
            r, c, seed(r)(c), tCh(r)(c), rCh(r)(c), inCh, outCh
          )
        }
      }

  private def makeNeighbours(dimensions: Dimensions, grid: Array[Array[CellOptions]]): Task[Unit] =
    dimensions.forEachRowCol { (r, c) =>
      val cell = grid(r)(c)
      dimensions.forEachNeighbor(r, c) { (ri, ci) =>
        val other = grid(ri)(ci)
        for
          ch <- Queue.unbounded[Boolean]
          _ <- cell.inChannels.update(_ :+ ch)
          _ <- other.outChannels.update(_ :+ ch)
        yield ()
      }
    }

  private def makeGrid[T](rows: Int, cols: Int): Task[List[List[Queue[T]]]] =
    ZIO.foreach(0 until rows)(_ =>
      ZIO.foreach(0 until cols)(_ =>
        Queue.unbounded[T]
      ).map(_.toList)
    ).map(_.toList)
