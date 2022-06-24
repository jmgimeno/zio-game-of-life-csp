package zgameoflife

import zio.*

case class CellOptions(
  row: Int,
  col: Int,
  alive: Boolean,
  tickChannel: Queue[Unit],
  resultChannel: Queue[Boolean],
  inChannels: Ref[List[Queue[Boolean]]],
  outChannels: Ref[List[Queue[Boolean]]]
)
