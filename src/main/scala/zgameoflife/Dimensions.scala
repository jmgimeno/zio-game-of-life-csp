package zgameoflife

import zio.*

case class Dimensions(rows: Int, cols: Int):

  def forEachRowCol(f: (Int, Int) => Task[Unit]): Task[Unit] =
    ZIO.foreachDiscard(0 until rows) { r =>
      ZIO.foreachDiscard(0 until cols) { c =>
        f(r, c)
      }
    }

  def forEachNeighbor(row: Int, col: Int)(f: (Int, Int) => Task[Unit]): Task[Unit] =
    ZIO.foreachDiscard((row - 1) to (row + 1)) { r =>
      ZIO.foreachDiscard((col - 1) to (col + 1)) { c =>
        f(r, c).when(r >= 0 && r < rows && c >= 0 && c < cols && (r != row || c != col))
      }
    }

