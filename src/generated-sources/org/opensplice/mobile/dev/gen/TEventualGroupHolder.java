package org.opensplice.mobile.dev.gen;

/**
* org/opensplice/mobile/dev/gen/TEventualGroupHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ./src/main/idl/TEventualGroup.idl
* Friday, July 26, 2013 6:51:30 PM CEST
*/

public final class TEventualGroupHolder implements org.omg.CORBA.portable.Streamable
{
  public org.opensplice.mobile.dev.gen.TEventualGroup value = null;

  public TEventualGroupHolder ()
  {
  }

  public TEventualGroupHolder (org.opensplice.mobile.dev.gen.TEventualGroup initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.opensplice.mobile.dev.gen.TEventualGroupHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.opensplice.mobile.dev.gen.TEventualGroupHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.opensplice.mobile.dev.gen.TEventualGroupHelper.type ();
  }

}
