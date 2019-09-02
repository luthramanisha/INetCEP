package ccn.packet

import org.scalatest.{Matchers, FlatSpec}

case class CCNPacketTest() extends FlatSpec with Matchers {

  "A CCNName beginning with 'COMPUTE'" should "yield true for isCompute" in {
    CCNName("COMPUTE", "cmp1", "cmp2").isCompute shouldBe true
  }

  "A CCNName not beginning with 'COMPUTE'" should "not yield true for isCompute" in {
    CCNName("cmp1", "cmp2").isCompute shouldBe false
  }

  "A CCNName ending with 'NFN'" should "yield true for isNFN" in {
    CCNName("cmp1", "cmp2", "NFN").isNFN shouldBe true
  }

  "A CCNName not ending with 'NFN'" should "not yield true for isNFN" in {
    CCNName("cmp1", "cmp2").isNFN shouldBe false
  }

  "A CCNName ending with 'THUNK/NFN'" should "yield true for isThunk" in {
    CCNName("cmp1", "cmp2", "THUNK", "NFN").isThunk shouldBe true
  }

  "A CCNName ending with 'THUNK'" should "not yield true for isThunk, because thunks are only allowed for NFN names" in {
    CCNName("cmp1", "cmp2", "THUNK").isThunk shouldBe false
  }
  "A CCNName not ending with 'THUNK/NFN'" should "not yield true for isThunk" in {
    CCNName("cmp1", "cmp2", "NFN").isThunk shouldNot be(true)
  }
}
