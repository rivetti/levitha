package org.opensplice.mobile.dev.paxos

object TestData {
  def state2Wire(value: StatePaxosData) = {
    new WireTestData(value.asInstanceOf[TestData].data)
  }

  def wire2State(value: WirePaxosData) = {
    new TestData(value.asInstanceOf[TestData].data)
  }
}

case class TestData(var data: String) extends StatePaxosData {
  override def toString(): String = "{Test Data: " + data + "}"

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[TestData]) {
      val that = obj.asInstanceOf[TestData]

      if (this.data.equals(that.data)) {
        return true
      }

    }
    return false
  }

  override def add(value: StatePaxosData): StatePaxosData = {
    this.data = " + " + value.asInstanceOf[TestData].data
    this
  }

  override def remove(value: StatePaxosData): StatePaxosData = {
    this.data = " - " + value.asInstanceOf[TestData].data
    this
  }

  override def update(add: List[(Int, StatePaxosData)], rmv: List[(Int, StatePaxosData)]) = {
    add.foreach(x => this.data = " + " + x.asInstanceOf[TestData].data)
    rmv.foreach(x => this.data = " - " + x.asInstanceOf[TestData].data)
    this
  }
}