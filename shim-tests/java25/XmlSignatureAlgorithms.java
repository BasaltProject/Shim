/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.*;
import java.util.*;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.*;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public class XmlSignatureAlgorithms {
  static final String CERT = "MIIC4zCCAcugAwIBAgIILPP/sbn4l30wDQYJKoZIhvcNAQEMBQAwIDEPMA0GA1UEChMGQmFzYWx0MQ0wCwYDVQQDEwRUZXN0MB4XDTI2MTEwNzEwMDc0M1oXDTM2MTEwNDEwMDc0M1owIDEPMA0GA1UEChMGQmFzYWx0MQ0wCwYDVQQDEwRUZXN0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy+pzURQqlMk3Zuk3whrJZGyRoj2q1QYIbCkUcysOka4uE/rccolkziUxxuPFddYnQ7eDOqT26y1aMCboMmiKYn8iNxgVufrRWdtUXiypFXoqdo4XQ8eNjiTbycFYDbxLpLko/lTdGGNyt9G3wcRA8i3CWDWqn0nGh1Xc3QCyNmazEf0zPbk9/uWPP1PcVAJo1jqPP1LfxmIo3NGCnOIlafU78FpOjOlRLmsdhP0VZ7A2DKnFoIf+67qBwAPslJFsprui+7a+jOWw247RsBiHhJr0HH9b2OjXjN5z5PPDH1rI4Eh1u9hcUIPB1xvoe81UZ7qmKqVJ/9+zb5g77NBdhQIDAQABoyEwHzAdBgNVHQ4EFgQUuQjkKnJgf4auhmbBg0IiDQQq60wwDQYJKoZIhvcNAQEMBQADggEBAJ0c+Kmvdxlc4L+3Koqwu9HFRQdBrqvKBrNlPSt3daRvFPnaReorIvrcAoa/nAv9IBIIdA4yUhBWFKdOLzBSn/Z3LtBdthoun2vjhxsiMAZxa/qebwXqQFqDtWYtdayxx/pbMtRXNWuOlzxXlazhdJPhgc7EabVuVtJnG+QaHDKm2CpJGHk287oeBJ7IU7sciA5/U8BwbLQjjyNVu9lLjfx2FTeF6d0KotbrteiBkBVhfJxc0067q82lkdhOYuMhKfE5wQUTrrWOwu8SsWFHzyWwTxHSocVTmWS0VDRxrjFuI+KlqVjEpvcyXreglHszV8M2soo4YcVZ9qozxvchJeQ=";
  static final String CERT_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDL6nNRFCqUyTdm6TfCGslkbJGiParVBghsKRRzKw6Rri4T+txyiWTOJTHG48V11idDt4M6pPbrLVowJugyaIpifyI3GBW5+tFZ21ReLKkVeip2jhdDx42OJNvJwVgNvEukuSj+VN0YY3K30bfBxEDyLcJYNaqfScaHVdzdALI2ZrMR/TM9uT3+5Y8/U9xUAmjWOo8/Ut/GYijc0YKc4iVp9TvwWk6M6VEuax2E/RVnsDYMqcWgh/7ruoHAA+yUkWymu6L7tr6M5bDbjtGwGIeEmvQcf1vY6NeM3nPk88MfWsjgSHW72FxQg8HXG+h7zVRnuqYqpUn/37NvmDvs0F2FAgMBAAECggEAIlQo77heZsftLMUMXwAVcXOZnN+W19/5IQRVbgIMpNfW6AQugpXAw7edRqdnwau41nyZehD1hKgQ69RQ+NOCoIxYnT/PwIyKG/+Bl40bNgNoDKdlIUMCALER00haY42UinmlfhOZWRa85FdJATTh3iN3+VOnksNfyb7ZvNb2KZQZfL0FcxQpo0xPeMnjKB3VopFLVbn4b7dtVwb7v0fpJKHQKs+coKRLHvzdboMM8+kVEM+5M4XcDx44ZW0mlN0pFGdSiAHBprU258K90bsHogO5nreLcNfkbUG+WOCl1fsUPG4kcICq+L+Avf9OduGLw1caJROOg6ORDrEDJNAz7QKBgQDYbvKq5sTJAZlYcLMecXaSh+1BQSvUk5/slqmXYEAayOhTD13W5TWJ28XPilBXU6SLMeL99CSX2gmWulaf3KUu6vNH/Zaxbmfhowg8995FmZ9WpKdXMuthPtlnaGoxhpF58tal0HEfXe1S0hw3EhAzVzDb2ENqYydM7xJFV2xRiwKBgQDxMa6e5AnzHGQeXzitUV1SFc3UOOs+2w3aZItlWUYIBIvhvbMK5Wyksn+FK7EYCaZXBjHE7gFbHrmJpZp1ZKce0Fq+0/tOMW2lVvn04rdKaMI4FV2wCnOs/bW0C4PeD0libfU25zlmoks4ksq7mEi0RJARLhOMnC9M20rSgbrPLwKBgAtOwJ7tL5rQ2pIkUTdl9ivxrhOatA+HJvPVa0bdYHwFwx2Tz/NKRmHNeag+MbReSlp7YMCj4MWOPrIBSkVP4dFxtQQt25GpkwPW4LdOu+4m+IwuTg/gOSv7fKBKdEHiJfeZGF3RZhedcKjDEXZPG0mTZJoX0m3cgFEkrG+zAZXBAoGBAI2BqoOKItRBSfQPxb7Zx7iLFlF2e35LIpuI9cemUtINaMwuccbox47xMqO8+I+zyaWw+q4hPSZqpkw9nptVTD9l2nevAvfjRAMHPmsQQJ0lTR1+W5MW7rsFtv9E6j70FQfsnztw+VpfyWYNqWfadn1c5HFP6SRL1exvlDxg5Gr9AoGBAMZMjIF12pu7IU2MYKmqwvpoFUWrEnd6HO5cpafZ3Rx2J+ay06rT61dhUuxedf+SeRcZm1NENFgTPwD8T6i4biDOwAapCTNA8cpDm4E4GFXCl9g1Sl11CC/jG1Gwbxz9hIZYQ1CyL3zLqNd0v/uZIAk9ow0dnYzenllrjHxRX8bR";
  static final String[][] KEYS = {
    {"ECsecp256r1", "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCANI3/NwWVwsC/30WQxFd1JTbmctxENbzUzRYOKTWElgw==", "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEETEpe/Pd+t2wnVJyFfHea0G9imMPh4mUyvufWne9e9E04eIEHfaXxI/mhHUcjawv9fGh17iOGquJ1Fgj4dchjw=="},
    {"ECsecp384r1", "ME4CAQAwEAYHKoZIzj0CAQYFK4EEACIENzA1AgEBBDB7cgNxA5IUlNv1L/V0SN3rYJy30K1aQp0AMpzapjYyvuQeM7yib8bh/Jg2QPa3d0Q=", "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEBAXFtxLuhVglSBs2oABGx6hmqFse24do8YsZF8l9Avwpk/cWTihyBxm9GqiO/zEqLAuP96oDQUnXDUn0cSWAjw7qyuPGyHLhFHbOkCWCILaLuRerz3oFyd1U3OGqZ8pS"},
    {"ECsecp521r1", "MGACAQAwEAYHKoZIzj0CAQYFK4EEACMESTBHAgEBBEIAPneYvuJ72U1XOmyW175K7wxnp/INdsHgzMrf8FWYwqE9d/PT1aVMw+XsInfRZI2xLB8IgoIO1xEi84Z/+dromKY=", "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQAWosUjY/dDsOSA43LiGaZC1PxGe0LYYuLzxx7IfkDN9fzXuW61+94VQvXsSfYqQfJS2I/MV+Gi4elsleYx1keVeoB2pgNbgst+3kfjaa32TWHz5IbRf5kC03DPRuQzMdSHRRLp+NEDiqHZL4dgSBQ32Hj5GZPQblOh44Hzg+ThMWxdpo="},
    {"DSA2048", "MIICXAIBADCCAjUGByqGSM44BAEwggIoAoIBAQCPeTXZuarpv6vtiHrPSVG28y7FnjuvNxjo6sSWHz79NgbnQ1GpxBgzObgJ58KuHFObp0dbhdARrbi0eYd1SYRpXKwOjxSzNggooi/6JxEKPWKpk0U0CaD+aWxGWPhL3SCBnDcJoBBXsZWtzQAjPbpUhLYpH51kjviDRIZ3l5zsBLQ0pqwudemYXeI9sCkvwRGMn/qdgYHnM423krcw17njSVkvaAmYchU5Feo9a4tGU8YzRY+AOzKkwuDycpAlbk4/ijsIOKHEUOThjBopo33fXqFD3ktm/wSQPtXPFiPhWNSHxgjpfyEc2B3KI8tuOAdl+CLjQr5ITAV2OTlgHNZnAh0AuvaWpoV499/e5/pnyXfHhe8ysjO65YDAvNVpXQKCAQAWplxYIEhQcE51AqOXVwQNNNo6NHjBVNTkpcAtJC7gT5bmHkvQkEq9rI837rHgnzGC0jyQQ8tkL4gAQWDt+coJsyB2p5wypifyRz6Rh5uixOdEvSCBVEy1W4AsNo0fqD7UielOD6BojjJCilx4xHjGjQUntxyaOrsLC+EsRGiWOefTznTbEBplqiuH9kxoJts+xy9LVZmDS7TtsC98kOmkltOlXVNb6/xF1PYZ9j897buHOSXC8iTgdzEpbaiH7B5HSPh++1/et1SEMWsiMt7lU92vAhErDR8C2jCXMiT+J67ai51LKSLZuovjntnhA6Y8UoELxoi34u1DFuHvF9veBB4CHD2eCOGDZZzwZeId1y86p0B8DIiV1D0qM13Gcoc=", "MIIDQjCCAjUGByqGSM44BAEwggIoAoIBAQCPeTXZuarpv6vtiHrPSVG28y7FnjuvNxjo6sSWHz79NgbnQ1GpxBgzObgJ58KuHFObp0dbhdARrbi0eYd1SYRpXKwOjxSzNggooi/6JxEKPWKpk0U0CaD+aWxGWPhL3SCBnDcJoBBXsZWtzQAjPbpUhLYpH51kjviDRIZ3l5zsBLQ0pqwudemYXeI9sCkvwRGMn/qdgYHnM423krcw17njSVkvaAmYchU5Feo9a4tGU8YzRY+AOzKkwuDycpAlbk4/ijsIOKHEUOThjBopo33fXqFD3ktm/wSQPtXPFiPhWNSHxgjpfyEc2B3KI8tuOAdl+CLjQr5ITAV2OTlgHNZnAh0AuvaWpoV499/e5/pnyXfHhe8ysjO65YDAvNVpXQKCAQAWplxYIEhQcE51AqOXVwQNNNo6NHjBVNTkpcAtJC7gT5bmHkvQkEq9rI837rHgnzGC0jyQQ8tkL4gAQWDt+coJsyB2p5wypifyRz6Rh5uixOdEvSCBVEy1W4AsNo0fqD7UielOD6BojjJCilx4xHjGjQUntxyaOrsLC+EsRGiWOefTznTbEBplqiuH9kxoJts+xy9LVZmDS7TtsC98kOmkltOlXVNb6/xF1PYZ9j897buHOSXC8iTgdzEpbaiH7B5HSPh++1/et1SEMWsiMt7lU92vAhErDR8C2jCXMiT+J67ai51LKSLZuovjntnhA6Y8UoELxoi34u1DFuHvF9veA4IBBQACggEAHpHKYDmIURcJl5eBHvnhCrFILcWHAsvpcSw3PQBFwAO5qarYg+1cHr8h1Vkv3mYog9hZVpuuetDZr+ukDQDt0AslVm2rwLsRUeXrJms+aNH6VnGvcUQMo/gER7LjWaWzumb/mSp5p90bVj4II3uCAoY9PjxH87Kd4t9gcTAJKLJAN/meG15FlG3ENDgz3e5WyP8Op2FlHFxF2ceUUcW2T+WLRWOQlRmRW/lsghIo6Ot4g3eKYqRbvCmr/w5CC/9QsdeACVUlGWIVzdCdeJ3WbJIEJkiVDy2Up9M7bC65EEfRSLWrIgPEVh0guZQRQ6bReLfymenOlvSsb0Z/iNm76w=="},
    {"Ed25519", "MC4CAQAwBQYDK2VwBCIEIKVdcMlUEWtC5YZQ+L69Y5xURd9RYc8agj/rFDow4eme", "MCowBQYDK2VwAyEAj9qtRfjOGM2KY/tYMTw4ZXLXDzHQu6RZGsypyrQdsiA="},
    {"RSA2048", "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"},
  };
  static final String[][] SIGNED = {
    {"ECsecp256r1", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", "<doc xmlns=\"urn:d\"><a>1</a><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><DigestValue>ILkZ4xpscdSGMFKXu7B/+VSUCNa7R/N87/B29X00Msg=</DigestValue></Reference></SignedInfo><SignatureValue>fZrIzuRQHNGjeXtKyocOtWAnKPCLJL09Wqwb9piO/VgHtL91cPop5jnHxaFTOMuDyRp3K0ZEGqDe&#13;+YDbV5lFzA==</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns=\"http://www.w3.org/2009/xmldsig11#\"><NamedCurve URI=\"urn:oid:1.2.840.10045.3.1.7\"/><PublicKey>BBExKXvz3frdsJ1SchXx3mtBvYpjD4eJlMr7n1p3vXvRNOHiBB32l8SP5oR1HI2sL/Xxode4jhqr&#13;idRYI+HXIY8=</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature></doc>"},
    {"ECsecp384r1", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384", "<doc xmlns=\"urn:d\"><a>1</a><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><DigestValue>ILkZ4xpscdSGMFKXu7B/+VSUCNa7R/N87/B29X00Msg=</DigestValue></Reference></SignedInfo><SignatureValue>p9RjPkpkFCcT7Wsh2RGeQvaIPNUo5hv5R9d9xHbIGq7nJWY1I5TzNwfCkhrZjnFoAsK7xDh7xZCu&#13;GdH3U829ykOYCbwyY3+kqx/CKeQm+BlE803sf4qZ56ctyvUhHjlz</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns=\"http://www.w3.org/2009/xmldsig11#\"><NamedCurve URI=\"urn:oid:1.3.132.0.34\"/><PublicKey>BAQFxbcS7oVYJUgbNqAARseoZqhbHtuHaPGLGRfJfQL8KZP3Fk4ocgcZvRqojv8xKiwLj/eqA0FJ&#13;1w1J9HElgI8O6srjxshy4RR2zpAlgiC2i7kXq896BcndVNzhqmfKUg==</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature></doc>"},
    {"ECsecp521r1", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512", "<doc xmlns=\"urn:d\"><a>1</a><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><DigestValue>ILkZ4xpscdSGMFKXu7B/+VSUCNa7R/N87/B29X00Msg=</DigestValue></Reference></SignedInfo><SignatureValue>AC7OKunmze7rgEe3ae5VTxp7QOqiLy0B9l9RD0p4ajFKsbCTfj95u+WvuB6vI2l7TTks5DL+uLra&#13;epg1gqMe9Jj+ADaXRZ/946gUMxzTXkbAqSv+4sfRPiduZy/EAPZXq+GxdkD9+faod110aENcujdj&#13;8mDzY/9TO8U7SAGV7bUp2vOh</SignatureValue><KeyInfo><KeyValue><ECKeyValue xmlns=\"http://www.w3.org/2009/xmldsig11#\"><NamedCurve URI=\"urn:oid:1.3.132.0.35\"/><PublicKey>BABaixSNj90Ow5IDjcuIZpkLU/EZ7Qthi4vPHHsh+QM31/Ne5brX73hVC9exJ9ipB8lLYj8xX4aL&#13;h6WyV5jHWR5V6gHamA1uCy37eR+NprfZNYfPkhtF/mQLTcM9G5DMx1IdFEun40QOKodkvh2BIFDf&#13;YePkZk9BuU6HjgfOD5OExbF2mg==</PublicKey></ECKeyValue></KeyValue></KeyInfo></Signature></doc>"},
    {"DSA2048", "http://www.w3.org/2009/xmldsig11#dsa-sha256", "<doc xmlns=\"urn:d\"><a>1</a><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><SignatureMethod Algorithm=\"http://www.w3.org/2009/xmldsig11#dsa-sha256\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><DigestValue>ILkZ4xpscdSGMFKXu7B/+VSUCNa7R/N87/B29X00Msg=</DigestValue></Reference></SignedInfo><SignatureValue>Mq3jCTlF2Z48jxKgC5pXlmG7//py9aJTuUInFmMVULE/Q+WRcKRQaKO68h6rU5sMMnS8b2DsAkE=</SignatureValue><KeyInfo><KeyValue><DSAKeyValue><P>j3k12bmq6b+r7Yh6z0lRtvMuxZ47rzcY6OrElh8+/TYG50NRqcQYMzm4CefCrhxTm6dHW4XQEa24&#13;tHmHdUmEaVysDo8UszYIKKIv+icRCj1iqZNFNAmg/mlsRlj4S90ggZw3CaAQV7GVrc0AIz26VIS2&#13;KR+dZI74g0SGd5ec7AS0NKasLnXpmF3iPbApL8ERjJ/6nYGB5zONt5K3MNe540lZL2gJmHIVORXq&#13;PWuLRlPGM0WPgDsypMLg8nKQJW5OP4o7CDihxFDk4YwaKaN9316hQ95LZv8EkD7VzxYj4VjUh8YI&#13;6X8hHNgdyiPLbjgHZfgi40K+SEwFdjk5YBzWZw==</P><Q>uvaWpoV499/e5/pnyXfHhe8ysjO65YDAvNVpXQ==</Q><G>FqZcWCBIUHBOdQKjl1cEDTTaOjR4wVTU5KXALSQu4E+W5h5L0JBKvayPN+6x4J8xgtI8kEPLZC+I&#13;AEFg7fnKCbMgdqecMqYn8kc+kYebosTnRL0ggVRMtVuALDaNH6g+1InpTg+gaI4yQopceMR4xo0F&#13;J7ccmjq7CwvhLERoljnn08502xAaZaorh/ZMaCbbPscvS1WZg0u07bAvfJDppJbTpV1TW+v8RdT2&#13;GfY/Pe27hzklwvIk4HcxKW2oh+weR0j4fvtf3rdUhDFrIjLe5VPdrwIRKw0fAtowlzIk/ieu2oud&#13;Syki2bqL457Z4QOmPFKBC8aIt+LtQxbh7xfb3g==</G><Y>HpHKYDmIURcJl5eBHvnhCrFILcWHAsvpcSw3PQBFwAO5qarYg+1cHr8h1Vkv3mYog9hZVpuuetDZ&#13;r+ukDQDt0AslVm2rwLsRUeXrJms+aNH6VnGvcUQMo/gER7LjWaWzumb/mSp5p90bVj4II3uCAoY9&#13;PjxH87Kd4t9gcTAJKLJAN/meG15FlG3ENDgz3e5WyP8Op2FlHFxF2ceUUcW2T+WLRWOQlRmRW/ls&#13;ghIo6Ot4g3eKYqRbvCmr/w5CC/9QsdeACVUlGWIVzdCdeJ3WbJIEJkiVDy2Up9M7bC65EEfRSLWr&#13;IgPEVh0guZQRQ6bReLfymenOlvSsb0Z/iNm76w==</Y></DSAKeyValue></KeyValue></KeyInfo></Signature></doc>"},
    {"Ed25519", "http://www.w3.org/2021/04/xmldsig-more#eddsa-ed25519", "<doc xmlns=\"urn:d\"><a>1</a><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><SignatureMethod Algorithm=\"http://www.w3.org/2021/04/xmldsig-more#eddsa-ed25519\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><DigestValue>ILkZ4xpscdSGMFKXu7B/+VSUCNa7R/N87/B29X00Msg=</DigestValue></Reference></SignedInfo><SignatureValue>K429AFNr2ipznf/MYY9OtPRBQSZNLoGQZVJDv6hlgQ1YJN1bQ5eOtAPpHGXF2S9BAL1NQsmYdc1K&#13;EVK+CJTeAA==</SignatureValue></Signature></doc>"},
    {"RSA2048", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "<doc xmlns=\"urn:d\"><a>1</a><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><DigestValue>ILkZ4xpscdSGMFKXu7B/+VSUCNa7R/N87/B29X00Msg=</DigestValue></Reference></SignedInfo><SignatureValue>iIt6dxMINbhh9C+uJlcHaJOUNLs0LHVBQvw13HP0WL61GH6zmFBAtFKJvwjVIE4K0iTonAjwxjkv&#13;TdxBPlkFwnjkRYldTvAvcX7FWDtNhjXOAat2I+LWvPBVWsR9KM5TTPiTksyfyJvRvnaxYRMbMtmx&#13;g7rrD8YGK1STDG4z0wTZv2vRn95/cxiyRmdF6p3yALmOT0NeBPH6FhEXU5tmIgBFFL/Oa9F6hMqk&#13;4Ip5IFoYrVhrju2+0dYJhNrJUE0OZCoM+NtIIPqKqcT86E+15mPIKQfgEXWScFZVsZERfYjsCLdx&#13;QFq6rnPDRQm3fl678AslCCPUuBwYjhbngzsz5A==</SignatureValue><KeyInfo><KeyValue><RSAKeyValue><Modulus>o5XCdrueDz25GByMsYbmBHcGZbjbLJ2eILrkJfo4NZzrc3VaDpZ16rxDDTtrBsv8B6tjAp6okfmr&#13;V1YLiUhSkr0yYeD464UWBjCsM/Z91we9NPI/acxoIYzYya24o6MOeWjihLQ+a4MmoautXjLEL/Cs&#13;QPzg4/Uuyd848ngDzqobOgpIXWybRsnNmvn2Xq1fTFYDrGLwJnI9jDBiHXf+UdhZg0GrxDaqNwxp&#13;LsmxnaPNFDn0ku1fRWLAa+rvZmNMt+FsOG5QYws82hnInDFkG8y+cmv9GVszXMQLHemYymWz0ieM&#13;kyzFONBIGkhsHrzA8kNRwhPsd7q1Amd3UJxm0Q==</Modulus><Exponent>AQAB</Exponent></RSAKeyValue></KeyValue></KeyInfo></Signature></doc>"},
    {"RSA2048+x509", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "<doc xmlns=\"urn:d\"><a>1</a><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><DigestValue>ILkZ4xpscdSGMFKXu7B/+VSUCNa7R/N87/B29X00Msg=</DigestValue></Reference></SignedInfo><SignatureValue>XBmXH5K3hOvB13oMuAR0KXjZQZtbHWfMbFwCb4WRVsR4h/RUruqqMpNAo22mVRrSkMMIBS9O2NRE&#13;o8A2xsQ+P97MCvqXrf50rcdHDMn2Ej/92EJRCngowBp5Z1HWsK+++RidfdkcleCQ3skVpfMDSW3v&#13;Rnrgne3CLeTZiU0EpBvMoMa9OaFUz4svWk/kNOrfFdXWfdIm9Y7CC+t+WL4bQru5iYTcdXbxrKd1&#13;gQyO5WNGFmAAksJ1WnVxxOs+G7z9rRkAktuFqNi1GBr4IxPhZ8Gck2/pKVNKjqS013goOh/aSFbi&#13;SRGEza8fW4eq9hvLjb88NAN5RkIns2WYDrWtRQ==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=Test,O=Basalt</X509SubjectName><X509Certificate>MIIC4zCCAcugAwIBAgIILPP/sbn4l30wDQYJKoZIhvcNAQEMBQAwIDEPMA0GA1UEChMGQmFzYWx0&#13;MQ0wCwYDVQQDEwRUZXN0MB4XDTI2MTEwNzEwMDc0M1oXDTM2MTEwNDEwMDc0M1owIDEPMA0GA1UE&#13;ChMGQmFzYWx0MQ0wCwYDVQQDEwRUZXN0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA&#13;y+pzURQqlMk3Zuk3whrJZGyRoj2q1QYIbCkUcysOka4uE/rccolkziUxxuPFddYnQ7eDOqT26y1a&#13;MCboMmiKYn8iNxgVufrRWdtUXiypFXoqdo4XQ8eNjiTbycFYDbxLpLko/lTdGGNyt9G3wcRA8i3C&#13;WDWqn0nGh1Xc3QCyNmazEf0zPbk9/uWPP1PcVAJo1jqPP1LfxmIo3NGCnOIlafU78FpOjOlRLmsd&#13;hP0VZ7A2DKnFoIf+67qBwAPslJFsprui+7a+jOWw247RsBiHhJr0HH9b2OjXjN5z5PPDH1rI4Eh1&#13;u9hcUIPB1xvoe81UZ7qmKqVJ/9+zb5g77NBdhQIDAQABoyEwHzAdBgNVHQ4EFgQUuQjkKnJgf4au&#13;hmbBg0IiDQQq60wwDQYJKoZIhvcNAQEMBQADggEBAJ0c+Kmvdxlc4L+3Koqwu9HFRQdBrqvKBrNl&#13;PSt3daRvFPnaReorIvrcAoa/nAv9IBIIdA4yUhBWFKdOLzBSn/Z3LtBdthoun2vjhxsiMAZxa/qe&#13;bwXqQFqDtWYtdayxx/pbMtRXNWuOlzxXlazhdJPhgc7EabVuVtJnG+QaHDKm2CpJGHk287oeBJ7I&#13;U7sciA5/U8BwbLQjjyNVu9lLjfx2FTeF6d0KotbrteiBkBVhfJxc0067q82lkdhOYuMhKfE5wQUT&#13;rrWOwu8SsWFHzyWwTxHSocVTmWS0VDRxrjFuI+KlqVjEpvcyXreglHszV8M2soo4YcVZ9qozxvch&#13;JeQ=</X509Certificate></X509Data></KeyInfo></Signature></doc>"},
  };

  static Document parse(String s) throws Exception {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    return f.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
  }

  static String serialize(Document d) throws Exception {
    Transformer t = TransformerFactory.newInstance().newTransformer();
    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    StringWriter w = new StringWriter();
    t.transform(new DOMSource(d), new StreamResult(w));
    return w.toString();
  }

  static String alg(String key) {
    return key.startsWith("EC") ? "EC" : key.startsWith("DSA") ? "DSA" : key.startsWith("Ed") ? "Ed25519" : "RSA";
  }

  static KeySelector selector(PublicKey fallback) {
    return new KeySelector() {
      public KeySelectorResult select(KeyInfo info, Purpose purpose, AlgorithmMethod method, XMLCryptoContext ctx)
          throws KeySelectorException {
        if (info != null) {
          for (Object o : info.getContent()) {
            if (o instanceof KeyValue) {
              try {
                final PublicKey k = ((KeyValue) o).getPublicKey();
                return () -> k;
              } catch (KeyException e) {
                throw new KeySelectorException(e);
              }
            }
            if (o instanceof X509Data) {
              for (Object c : ((X509Data) o).getContent()) {
                System.out.println("  x509 content " + c.getClass().getSimpleName().replaceAll("Impl|X509.*", "X509")
                    + (c instanceof String ? " " + c : ""));
                if (c instanceof X509Certificate) {
                  final PublicKey k = ((X509Certificate) c).getPublicKey();
                  return () -> k;
                }
              }
            }
          }
        }
        if (fallback != null) {
          return () -> fallback;
        }
        throw new KeySelectorException("no key");
      }
    };
  }

  static void check(String name, String xml, PublicKey fallback) throws Exception {
    Document doc = parse(xml);
    DOMValidateContext vc = new DOMValidateContext(selector(fallback),
        doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
    XMLSignature s = fac.unmarshalXMLSignature(vc);
    boolean v = s.validate(vc);
    System.out.println(name + " valid=" + v + " sv=" + s.getSignatureValue().validate(vc) + " method="
        + s.getSignedInfo().getSignatureMethod().getAlgorithm());
    if (!v) {
      return;
    }
    Document tampered = parse(xml.replace("<a>1</a>", "<a>2</a>"));
    DOMValidateContext tc = new DOMValidateContext(selector(fallback),
        tampered.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
    System.out.println(name + " tampered=" + fac.unmarshalXMLSignature(tc).validate(tc));
  }

  public static void main(String[] args) throws Exception {
    for (String[] s : SIGNED) {
      String key = s[0].replace("+x509", "");
      PublicKey pub = null;
      for (String[] k : KEYS) {
        if (k[0].equals(key)) {
          pub = KeyFactory.getInstance(alg(key)).generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(k[2])));
        }
      }
      try {
        check("fixture " + s[0], s[2], pub);
      } catch (Exception e) {
        System.out.println("fixture " + s[0] + " !! " + e);
      }
    }
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
    KeyInfoFactory kif = fac.getKeyInfoFactory();
    String[] sigs = {"http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256",
        "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512",
        "http://www.w3.org/2009/xmldsig11#dsa-sha256", "http://www.w3.org/2021/04/xmldsig-more#eddsa-ed25519",
        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"};
    for (int i = 0; i < KEYS.length; i++) {
      String[] k = KEYS[i];
      KeyFactory kf = KeyFactory.getInstance(alg(k[0]));
      PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(k[1])));
      PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(k[2])));
      try {
        Document doc = parse("<doc xmlns=\"urn:d\"><a>1</a></doc>");
        Reference r = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null), Collections.singletonList(
            fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
        SignedInfo si = fac.newSignedInfo(
            fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
            fac.newSignatureMethod(sigs[i], null), Collections.singletonList(r));
        KeyInfo ki = null;
        try {
          ki = kif.newKeyInfo(Collections.singletonList(kif.newKeyValue(pub)));
        } catch (KeyException e) {
          System.out.println("keyvalue " + k[0] + " !! " + e.getMessage());
        }
        fac.newXMLSignature(si, ki).sign(new DOMSignContext(priv, doc.getDocumentElement()));
        check("self " + k[0], serialize(doc), pub);
      } catch (Exception e) {
        System.out.println("self " + k[0] + " !! " + e);
      }
    }
    PrivateKey certKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(CERT_KEY)));
    X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
        new ByteArrayInputStream(Base64.getDecoder().decode(CERT)));
    Document doc = parse("<doc xmlns=\"urn:d\"><a>1</a></doc>");
    Reference r = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null), Collections.singletonList(
        fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
    SignedInfo si = fac.newSignedInfo(
        fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
        fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), Collections.singletonList(r));
    KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kif.newX509Data(Arrays.asList(
        cert.getSubjectX500Principal().getName(), cert, kif.newX509IssuerSerial(cert.getIssuerX500Principal().getName(),
        cert.getSerialNumber())))));
    fac.newXMLSignature(si, ki).sign(new DOMSignContext(certKey, doc.getDocumentElement()));
    String out = serialize(doc);
    System.out.println(out.replaceAll("<SignatureValue>[^<]*</SignatureValue>", "<SignatureValue/>")
        .replaceAll("<X509Certificate>[^<]*", "<X509Certificate>"));
    check("self x509", out, null);
    check("tamper-sig", out.replace("<a>1</a>", "<a>1</a>").replaceFirst("<SignatureValue>[A-Za-z0-9]", "<SignatureValue>A"), null);
  }
}
