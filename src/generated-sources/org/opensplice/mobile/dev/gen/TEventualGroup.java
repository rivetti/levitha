package org.opensplice.mobile.dev.gen;


/**
* org/opensplice/mobile/dev/gen/TEventualGroup.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ./src/main/idl/TEventualGroup.idl
* Friday, July 26, 2013 6:51:30 PM CEST
*/

/**
* Updated by idl2j
* from ./src/main/idl/TEventualGroup.idl
* Friday, July 26, 2013 6:51:31 PM CEST
*/

import org.opensplice.mobile.dcps.keys.KeyList;

@KeyList(
    topicType = "TEventualGroup",
    keys = {"memberId.low", "memberId.high"}
)
public final class TEventualGroup implements org.omg.CORBA.portable.IDLEntity
{
  public org.opensplice.mobile.dev.gen.uuid memberId = null;

  public TEventualGroup ()
  {
  } // ctor

  public TEventualGroup (org.opensplice.mobile.dev.gen.uuid _memberId)
  {
    memberId = _memberId;
  } // ctor

} // class TEventualGroup
