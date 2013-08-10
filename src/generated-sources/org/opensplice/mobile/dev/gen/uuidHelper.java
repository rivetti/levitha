package org.opensplice.mobile.dev.gen;


/**
* org/opensplice/mobile/dev/gen/uuidHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ./src/main/idl/uuid.idl
* Friday, July 26, 2013 6:51:31 PM CEST
*/

abstract public class uuidHelper
{
  private static String  _id = "IDL:org/opensplice/mobile/dev/gen/uuid:1.0";

  public static void insert (org.omg.CORBA.Any a, org.opensplice.mobile.dev.gen.uuid that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static org.opensplice.mobile.dev.gen.uuid extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [2];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_longlong);
          _members0[0] = new org.omg.CORBA.StructMember (
            "high",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_longlong);
          _members0[1] = new org.omg.CORBA.StructMember (
            "low",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (org.opensplice.mobile.dev.gen.uuidHelper.id (), "uuid", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.opensplice.mobile.dev.gen.uuid read (org.omg.CORBA.portable.InputStream istream)
  {
    org.opensplice.mobile.dev.gen.uuid value = new org.opensplice.mobile.dev.gen.uuid ();
    value.high = istream.read_longlong ();
    value.low = istream.read_longlong ();
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.opensplice.mobile.dev.gen.uuid value)
  {
    ostream.write_longlong (value.high);
    ostream.write_longlong (value.low);
  }

}