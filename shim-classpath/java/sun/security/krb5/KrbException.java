/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

public class KrbException extends Exception {
  private static final long serialVersionUID = -4993302876451928596L;

  public static final int KDC_ERR_C_PRINCIPAL_UNKNOWN = 6;
  public static final int KDC_ERR_S_PRINCIPAL_UNKNOWN = 7;
  public static final int KDC_ERR_ETYPE_NOSUPP = 14;
  public static final int KDC_ERR_PREAUTH_FAILED = 24;
  public static final int KDC_ERR_PREAUTH_REQUIRED = 25;
  public static final int KRB_AP_ERR_BAD_INTEGRITY = 31;
  public static final int KRB_AP_ERR_TKT_EXPIRED = 32;
  public static final int KRB_AP_ERR_TKT_NYV = 33;
  public static final int KRB_AP_ERR_REPEAT = 34;
  public static final int KRB_AP_ERR_NOT_US = 35;
  public static final int KRB_AP_ERR_BADMATCH = 36;
  public static final int KRB_AP_ERR_SKEW = 37;
  public static final int KRB_AP_ERR_BADADDR = 38;
  public static final int KRB_AP_ERR_BADVERSION = 39;
  public static final int KRB_AP_ERR_MSG_TYPE = 40;
  public static final int KRB_AP_ERR_MODIFIED = 41;
  public static final int KRB_AP_ERR_BADORDER = 42;
  public static final int KRB_AP_ERR_BADKEYVER = 44;
  public static final int KRB_AP_ERR_NOKEY = 45;
  public static final int KRB_AP_ERR_MUT_FAIL = 46;
  public static final int KRB_AP_ERR_BADDIRECTION = 47;
  public static final int KRB_AP_ERR_METHOD = 48;
  public static final int KRB_AP_ERR_BADSEQ = 49;
  public static final int KRB_AP_ERR_INAPP_CKSUM = 50;
  public static final int KRB_ERR_RESPONSE_TOO_BIG = 52;
  public static final int KRB_ERR_GENERIC = 60;
  public static final int KRB_ERR_FIELD_TOOLONG = 61;
  public static final int KRB_CRYPTO_NOT_SUPP = 906;
  public static final int ASN1_BAD_ID = 906;

  private int returnCode;
  private byte[] errorData;

  public KrbException(String s) {
    super(s);
  }

  public KrbException(Throwable cause) {
    super(cause);
  }

  public KrbException(int i) {
    super(message(i));
    returnCode = i;
  }

  public KrbException(int i, String s) {
    super(message(i) + " - " + s);
    returnCode = i;
  }

  public static KrbException fromError(int code, String text, byte[] edata) {
    KrbException e = text == null ? new KrbException(code) : new KrbException(code, text);
    e.errorData = edata;
    e.initCause(new Asn1Exception(ASN1_BAD_ID));
    return e;
  }

  public String toString() {
    return "KrbException: " + getMessage();
  }

  public int returnCode() {
    return returnCode;
  }

  public byte[] errorData() {
    return errorData;
  }

  public String returnCodeSymbol() {
    return message(returnCode);
  }

  public static String message(int code) {
    return errorText(code) + " (" + code + ")";
  }

  public static String errorText(int code) {
    switch (code) {
      case 0:
        return "No error";
      case 1:
        return "Client's entry in database has expired";
      case 2:
        return "Server's entry in database has expired";
      case 3:
        return "Requested protocol version # not supported";
      case 4:
        return "Client's key encrypted in old master key";
      case 5:
        return "Server's key encrypted in old master key";
      case 6:
        return "Client not found in Kerberos database";
      case 7:
        return "Server not found in Kerberos database";
      case 8:
        return "Multiple principal entries in database";
      case 9:
        return "The client or server has a null key";
      case 10:
        return "Ticket not eligible for postdating";
      case 11:
        return "Requested start time is later than end time";
      case 12:
        return "KDC policy rejects request";
      case 13:
        return "KDC cannot accommodate requested option";
      case 14:
        return "KDC has no support for encryption type";
      case 15:
        return "KDC has no support for checksum type";
      case 16:
        return "KDC has no support for padata type";
      case 17:
        return "KDC has no support for transited type";
      case 18:
        return "Clients credentials have been revoked";
      case 19:
        return "Credentials for server have been revoked";
      case 20:
        return "TGT has been revoked";
      case 21:
        return "Client not yet valid - try again later";
      case 22:
        return "Server not yet valid - try again later";
      case 23:
        return "Password has expired";
      case 24:
        return "Pre-authentication information was invalid";
      case 25:
        return "Additional pre-authentication required";
      case 31:
        return "Integrity check on decrypted field failed";
      case 32:
        return "Ticket expired";
      case 33:
        return "Ticket not yet valid";
      case 34:
        return "Request is a replay";
      case 35:
        return "The ticket isn't for us";
      case 36:
        return "Ticket and authenticator don't match";
      case 37:
        return "Clock skew too great";
      case 38:
        return "Incorrect net address";
      case 39:
        return "Protocol version mismatch";
      case 40:
        return "Invalid msg type";
      case 41:
        return "Message stream modified";
      case 42:
        return "Message out of order";
      case 44:
        return "Specified version of key is not available";
      case 45:
        return "Service key not available";
      case 46:
        return "Mutual authentication failed";
      case 47:
        return "Incorrect message direction";
      case 48:
        return "Alternative authentication method required";
      case 49:
        return "Incorrect sequence number in message";
      case 50:
        return "Inappropriate type of checksum in message";
      case 52:
        return "Response too big for UDP, retry with TCP";
      case 60:
        return "Generic error";
      case 61:
        return "Field is too long for this implementation";
      case 62:
        return "Client not trusted";
      case 63:
        return "KDC not trusted";
      case 64:
        return "Invalid signature";
      case 65:
        return "Key parameters not accepted";
      case 66:
        return "Certificate mismatch";
      case 68:
        return "Wrong realm";
      case 906:
        return "Identifier doesn't match expected value";
      default:
        return "Unknown error";
    }
  }
}
