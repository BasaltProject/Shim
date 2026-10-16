/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

final class LocaleData {
  private LocaleData() { }

  static final String kIsoLanguages =
      "aa:aar ab:abk ae:ave af:afr ak:aka am:amh an:arg ar:ara as:asm av:ava ay:aym az:aze ba:bak be:bel bg" +
      ":bul bh:bih bi:bis bm:bam bn:ben bo:bod br:bre bs:bos ca:cat ce:che ch:cha co:cos cr:cre cs:ces cu:c" +
      "hu cv:chv cy:cym da:dan de:deu dv:div dz:dzo ee:ewe el:ell en:eng eo:epo es:spa et:est eu:eus fa:fas" +
      " ff:ful fi:fin fj:fij fo:fao fr:fra fy:fry ga:gle gd:gla gl:glg gn:grn gu:guj gv:glv ha:hau he:heb h" +
      "i:hin ho:hmo hr:hrv ht:hat hu:hun hy:hye hz:her ia:ina id:ind ie:ile ig:ibo ii:iii ik:ipk in:ind io:" +
      "ido is:isl it:ita iu:iku iw:heb ja:jpn ji:yid jv:jav ka:kat kg:kon ki:kik kj:kua kk:kaz kl:kal km:kh" +
      "m kn:kan ko:kor kr:kau ks:kas ku:kur kv:kom kw:cor ky:kir la:lat lb:ltz lg:lug li:lim ln:lin lo:lao " +
      "lt:lit lu:lub lv:lav mg:mlg mh:mah mi:mri mk:mkd ml:mal mn:mon mo:mol mr:mar ms:msa mt:mlt my:mya na" +
      ":nau nb:nob nd:nde ne:nep ng:ndo nl:nld nn:nno no:nor nr:nbl nv:nav ny:nya oc:oci oj:oji om:orm or:o" +
      "ri os:oss pa:pan pi:pli pl:pol ps:pus pt:por qu:que rm:roh rn:run ro:ron ru:rus rw:kin sa:san sc:srd" +
      " sd:snd se:sme sg:sag si:sin sk:slk sl:slv sm:smo sn:sna so:som sq:sqi sr:srp ss:ssw st:sot su:sun s" +
      "v:swe sw:swa ta:tam te:tel tg:tgk th:tha ti:tir tk:tuk tl:tgl tn:tsn to:ton tr:tur ts:tso tt:tat tw:" +
      "twi ty:tah ug:uig uk:ukr ur:urd uz:uzb ve:ven vi:vie vo:vol wa:wln wo:wol xh:xho yi:yid yo:yor za:zh" +
      "a zh:zho zu:zul";

  static final String kIsoCountries =
      "AD:AND AE:ARE AF:AFG AG:ATG AI:AIA AL:ALB AM:ARM AO:AGO AQ:ATA AR:ARG AS:ASM AT:AUT AU:AUS AW:ABW AX" +
      ":ALA AZ:AZE BA:BIH BB:BRB BD:BGD BE:BEL BF:BFA BG:BGR BH:BHR BI:BDI BJ:BEN BL:BLM BM:BMU BN:BRN BO:B" +
      "OL BQ:BES BR:BRA BS:BHS BT:BTN BV:BVT BW:BWA BY:BLR BZ:BLZ CA:CAN CC:CCK CD:COD CF:CAF CG:COG CH:CHE" +
      " CI:CIV CK:COK CL:CHL CM:CMR CN:CHN CO:COL CR:CRI CU:CUB CV:CPV CW:CUW CX:CXR CY:CYP CZ:CZE DE:DEU D" +
      "J:DJI DK:DNK DM:DMA DO:DOM DZ:DZA EC:ECU EE:EST EG:EGY EH:ESH ER:ERI ES:ESP ET:ETH FI:FIN FJ:FJI FK:" +
      "FLK FM:FSM FO:FRO FR:FRA GA:GAB GB:GBR GD:GRD GE:GEO GF:GUF GG:GGY GH:GHA GI:GIB GL:GRL GM:GMB GN:GI" +
      "N GP:GLP GQ:GNQ GR:GRC GS:SGS GT:GTM GU:GUM GW:GNB GY:GUY HK:HKG HM:HMD HN:HND HR:HRV HT:HTI HU:HUN " +
      "ID:IDN IE:IRL IL:ISR IM:IMN IN:IND IO:IOT IQ:IRQ IR:IRN IS:ISL IT:ITA JE:JEY JM:JAM JO:JOR JP:JPN KE" +
      ":KEN KG:KGZ KH:KHM KI:KIR KM:COM KN:KNA KP:PRK KR:KOR KW:KWT KY:CYM KZ:KAZ LA:LAO LB:LBN LC:LCA LI:L" +
      "IE LK:LKA LR:LBR LS:LSO LT:LTU LU:LUX LV:LVA LY:LBY MA:MAR MC:MCO MD:MDA ME:MNE MF:MAF MG:MDG MH:MHL" +
      " MK:MKD ML:MLI MM:MMR MN:MNG MO:MAC MP:MNP MQ:MTQ MR:MRT MS:MSR MT:MLT MU:MUS MV:MDV MW:MWI MX:MEX M" +
      "Y:MYS MZ:MOZ NA:NAM NC:NCL NE:NER NF:NFK NG:NGA NI:NIC NL:NLD NO:NOR NP:NPL NR:NRU NU:NIU NZ:NZL OM:" +
      "OMN PA:PAN PE:PER PF:PYF PG:PNG PH:PHL PK:PAK PL:POL PM:SPM PN:PCN PR:PRI PS:PSE PT:PRT PW:PLW PY:PR" +
      "Y QA:QAT RE:REU RO:ROU RS:SRB RU:RUS RW:RWA SA:SAU SB:SLB SC:SYC SD:SDN SE:SWE SG:SGP SH:SHN SI:SVN " +
      "SJ:SJM SK:SVK SL:SLE SM:SMR SN:SEN SO:SOM SR:SUR SS:SSD ST:STP SV:SLV SX:SXM SY:SYR SZ:SWZ TC:TCA TD" +
      ":TCD TF:ATF TG:TGO TH:THA TJ:TJK TK:TKL TL:TLS TM:TKM TN:TUN TO:TON TR:TUR TT:TTO TV:TUV TW:TWN TZ:T" +
      "ZA UA:UKR UG:UGA UM:UMI US:USA UY:URY UZ:UZB VA:VAT VC:VCT VE:VEN VG:VGB VI:VIR VN:VNM VU:VUT WF:WLF" +
      " WS:WSM YE:YEM YT:MYT ZA:ZAF ZM:ZMB ZW:ZWE";

  static final String kIsoCountriesPart3 =
      "AIDJ ANHH BQAQ BUMM BYAA CSHH CSXX CTKI DDDE DYBJ FQHH FXFR GEHH HVBF JTUM MIUM NHVU NQAQ NTHH PCHH " +
      "PUUM PZPA RHZW SKIN SUHH TPTL VDVN WKUM YDYE YUCS ZRCD";

  static final String kAvailable =
      "af af-Latn-ZA af-NA af-ZA agq agq-CM agq-Latn-CM ak ak-GH ak-Latn-GH am am-ET am-Ethi-ET ann ann-Lat" +
      "n-NG ann-NG ar ar-001 ar-AE ar-Arab-EG ar-BH ar-DJ ar-DZ ar-EG ar-EH ar-ER ar-IL ar-IQ ar-JO ar-KM a" +
      "r-KW ar-LB ar-LY ar-MA ar-MR ar-OM ar-PS ar-QA ar-SA ar-SD ar-SO ar-SS ar-SY ar-TD ar-TN ar-YE as as" +
      "-Beng-IN as-IN asa asa-Latn-TZ asa-TZ ast ast-ES ast-Latn-ES az az-AZ az-Cyrl az-Cyrl-AZ az-Latn az-" +
      "Latn-AZ bal-Latn bal-Latn-PK bas bas-CM bas-Latn-CM be be-BY be-Cyrl-BY be-TARASK bem bem-Latn-ZM be" +
      "m-ZM bez bez-Latn-TZ bez-TZ bg bg-BG bg-Cyrl-BG bgc bgc-Deva-IN bgc-IN bho bho-Deva-IN bho-IN blo bl" +
      "o-BJ blo-Latn-BJ bm bm-Latn-ML bm-ML bn bn-BD bn-Beng-BD bn-IN bo bo-CN bo-IN bo-Tibt-CN br br-FR br" +
      "-Latn-FR brx brx-Deva-IN brx-IN bs bs-BA bs-Cyrl bs-Cyrl-BA bs-Latn bs-Latn-BA ca ca-AD ca-ES ca-ES-" +
      "VALENCIA ca-FR ca-IT ca-Latn-ES ccp ccp-BD ccp-Cakm-BD ccp-IN ce ce-Cyrl-RU ce-RU ceb ceb-Latn-PH ce" +
      "b-PH cgg cgg-Latn-UG cgg-UG chr chr-Cher-US chr-US ckb ckb-Arab-IQ ckb-IQ ckb-IR cs cs-CZ cs-Latn-CZ" +
      " csw csw-CA csw-Cans-CA cv cv-Cyrl-RU cv-RU cy cy-GB cy-Latn-GB da da-DK da-GL da-Latn-DK dav dav-KE" +
      " dav-Latn-KE de de-AT de-BE de-CH de-DE de-IT de-LI de-LU de-Latn-DE dje dje-Latn-NE dje-NE doi doi-" +
      "Deva-IN doi-IN dsb dsb-DE dsb-Latn-DE dua dua-CM dua-Latn-CM dyo dyo-Latn-SN dyo-SN dz dz-BT dz-Tibt" +
      "-BT ebu ebu-KE ebu-Latn-KE ee ee-GH ee-Latn-GH ee-TG el el-CY el-GR el-Grek-GR el-POLYTON en en-001 " +
      "en-150 en-AE en-AG en-AI en-AS en-AT en-AU en-BB en-BE en-BI en-BM en-BS en-BW en-BZ en-CA en-CC en-" +
      "CH en-CK en-CM en-CX en-CY en-CZ en-DE en-DG en-DK en-DM en-ER en-ES en-FI en-FJ en-FK en-FM en-FR e" +
      "n-GB en-GD en-GG en-GH en-GI en-GM en-GS en-GU en-GY en-HK en-HU en-ID en-IE en-IL en-IM en-IN en-IO" +
      " en-IT en-JE en-JM en-KE en-KI en-KN en-KY en-LC en-LR en-LS en-Latn-US en-MG en-MH en-MO en-MP en-M" +
      "S en-MT en-MU en-MV en-MW en-MY en-NA en-NF en-NG en-NL en-NO en-NR en-NU en-NZ en-PG en-PH en-PK en" +
      "-PL en-PN en-PR en-PT en-PW en-RO en-RW en-SB en-SC en-SD en-SE en-SG en-SH en-SI en-SK en-SL en-SS " +
      "en-SX en-SZ en-TC en-TK en-TO en-TT en-TV en-TZ en-UG en-UM en-US en-US-POSIX en-VC en-VG en-VI en-V" +
      "U en-WS en-ZA en-ZM en-ZW eo eo-001 eo-Latn-001 es es-419 es-AR es-BO es-BR es-BZ es-CL es-CO es-CR " +
      "es-CU es-DO es-EA es-EC es-ES es-GQ es-GT es-HN es-IC es-Latn-ES es-MX es-NI es-PA es-PE es-PH es-PR" +
      " es-PY es-SV es-US es-UY es-VE et et-EE et-Latn-EE eu eu-ES eu-Latn-ES ewo ewo-CM ewo-Latn-CM fa fa-" +
      "AF fa-Arab-IR fa-IR ff ff-Adlm ff-Adlm-BF ff-Adlm-CM ff-Adlm-GH ff-Adlm-GM ff-Adlm-GN ff-Adlm-GW ff-" +
      "Adlm-LR ff-Adlm-MR ff-Adlm-NE ff-Adlm-NG ff-Adlm-SL ff-Adlm-SN ff-GN ff-Latn ff-Latn-BF ff-Latn-CM f" +
      "f-Latn-GH ff-Latn-GM ff-Latn-GN ff-Latn-GW ff-Latn-LR ff-Latn-MR ff-Latn-NE ff-Latn-NG ff-Latn-SL ff" +
      "-Latn-SN ff-SN fi fi-FI fi-Latn-FI fil fil-Latn-PH fil-PH fo fo-DK fo-FO fo-Latn-FO fr fr-BE fr-BF f" +
      "r-BI fr-BJ fr-BL fr-CA fr-CD fr-CF fr-CG fr-CH fr-CI fr-CM fr-DJ fr-DZ fr-FR fr-GA fr-GF fr-GN fr-GP" +
      " fr-GQ fr-HT fr-KM fr-LU fr-Latn-FR fr-MA fr-MC fr-MF fr-MG fr-ML fr-MQ fr-MR fr-MU fr-NC fr-NE fr-P" +
      "F fr-PM fr-RE fr-RW fr-SC fr-SN fr-SY fr-TD fr-TG fr-TN fr-VU fr-WF fr-YT frr frr-DE frr-Latn-DE fur" +
      " fur-IT fur-Latn-IT fy fy-Latn-NL fy-NL ga ga-GB ga-IE ga-Latn-IE gaa gaa-GH gaa-Latn-GH gd gd-GB gd" +
      "-Latn-GB gl gl-ES gl-Latn-ES gsw gsw-CH gsw-FR gsw-LI gsw-Latn-CH gu gu-Gujr-IN gu-IN guz guz-KE guz" +
      "-Latn-KE gv gv-IM gv-Latn-IM ha ha-GH ha-Latn-NG ha-NE ha-NG haw haw-Latn-US haw-US he he-Hebr-IL he" +
      "-IL hi hi-Deva-IN hi-IN hi-Latn hi-Latn-IN hr hr-BA hr-HR hr-Latn-HR hsb hsb-DE hsb-Latn-DE ht ht-HT" +
      " ht-Latn-HT hu hu-HU hu-Latn-HU hy hy-AM hy-Armn-AM ia ia-001 ia-Latn-001 id id-ID id-Latn-ID ie ie-" +
      "EE ie-Latn-EE ig ig-Latn-NG ig-NG ii ii-CN ii-Yiii-CN is is-IS is-Latn-IS it it-CH it-IT it-Latn-IT " +
      "it-SM it-VA ja ja-JP ja-Jpan-JP ja_JP_JP jgo jgo-CM jgo-Latn-CM jmc jmc-Latn-TZ jmc-TZ jv jv-ID jv-L" +
      "atn-ID ka ka-GE ka-Geor-GE kab kab-DZ kab-Latn-DZ kam kam-KE kam-Latn-KE kde kde-Latn-TZ kde-TZ kea " +
      "kea-CV kea-Latn-CV kgp kgp-BR kgp-Latn-BR khq khq-Latn-ML khq-ML ki ki-KE ki-Latn-KE kk kk-Cyrl kk-C" +
      "yrl-KZ kk-KZ kkj kkj-CM kkj-Latn-CM kl kl-GL kl-Latn-GL kln kln-KE kln-Latn-KE km km-KH km-Khmr-KH k" +
      "n kn-IN kn-Knda-IN ko ko-CN ko-KP ko-KR ko-Kore-KR kok kok-Deva kok-Deva-IN kok-IN kok-Latn kok-Latn" +
      "-IN ks ks-Arab ks-Arab-IN ks-Deva ks-Deva-IN ks-IN ksb ksb-Latn-TZ ksb-TZ ksf ksf-CM ksf-Latn-CM ksh" +
      " ksh-DE ksh-Latn-DE ku ku-Latn-TR ku-TR kw kw-GB kw-Latn-GB kxv kxv-Deva kxv-Deva-IN kxv-IN kxv-Latn" +
      " kxv-Latn-IN kxv-Orya kxv-Orya-IN kxv-Telu kxv-Telu-IN ky ky-Cyrl-KG ky-KG lag lag-Latn-TZ lag-TZ lb" +
      " lb-LU lb-Latn-LU lg lg-Latn-UG lg-UG lij lij-IT lij-Latn-IT lkt lkt-Latn-US lkt-US lmo lmo-IT lmo-L" +
      "atn-IT ln ln-AO ln-CD ln-CF ln-CG ln-Latn-CD lo lo-LA lo-Laoo-LA lrc lrc-Arab-IR lrc-IQ lrc-IR lt lt" +
      "-LT lt-Latn-LT lu lu-CD lu-Latn-CD luo luo-KE luo-Latn-KE luy luy-KE luy-Latn-KE lv lv-LV lv-Latn-LV" +
      " mai mai-Deva-IN mai-IN mas mas-KE mas-Latn-KE mas-TZ mdf mdf-Cyrl-RU mdf-RU mer mer-KE mer-Latn-KE " +
      "mfe mfe-Latn-MU mfe-MU mg mg-Latn-MG mg-MG mgh mgh-Latn-MZ mgh-MZ mgo mgo-CM mgo-Latn-CM mi mi-Latn-" +
      "NZ mi-NZ mk mk-Cyrl-MK mk-MK ml ml-IN ml-Mlym-IN mn mn-Cyrl-MN mn-MN mni mni-Beng mni-Beng-IN mni-IN" +
      " mr mr-Deva-IN mr-IN ms ms-BN ms-ID ms-Latn-MY ms-MY ms-SG mt mt-Latn-MT mt-MT mua mua-CM mua-Latn-C" +
      "M my my-MM my-Mymr-MM mzn mzn-Arab-IR mzn-IR naq naq-Latn-NA naq-NA nb nb-Latn-NO nb-NO nb-SJ nd nd-" +
      "Latn-ZW nd-ZW nds nds-DE nds-Latn-DE nds-NL ne ne-Deva-NP ne-IN ne-NP nl nl-AW nl-BE nl-BQ nl-CW nl-" +
      "Latn-NL nl-NL nl-SR nl-SX nmg nmg-CM nmg-Latn-CM nn nn-Latn-NO nn-NO nnh nnh-CM nnh-Latn-CM no no-La" +
      "tn-NO no-NO no_NO_NY nqo nqo-GN nqo-Nkoo-GN nso nso-Latn-ZA nso-ZA nus nus-Latn-SS nus-SS nyn nyn-La" +
      "tn-UG nyn-UG oc oc-ES oc-FR oc-Latn-FR om om-ET om-KE om-Latn-ET or or-IN or-Orya-IN os os-Cyrl-GE o" +
      "s-GE os-RU pa pa-Arab pa-Arab-PK pa-Guru pa-Guru-IN pa-IN pa-PK pcm pcm-Latn-NG pcm-NG pis pis-Latn-" +
      "SB pis-SB pl pl-Latn-PL pl-PL ps ps-AF ps-Arab-AF ps-PK pt pt-AO pt-BR pt-CH pt-CV pt-GQ pt-GW pt-LU" +
      " pt-Latn-BR pt-MO pt-MZ pt-PT pt-ST pt-TL qu qu-BO qu-EC qu-Latn-PE qu-PE raj raj-Deva-IN raj-IN rm " +
      "rm-CH rm-Latn-CH rn rn-BI rn-Latn-BI ro ro-Latn-RO ro-MD ro-RO rof rof-Latn-TZ rof-TZ ru ru-BY ru-Cy" +
      "rl-RU ru-KG ru-KZ ru-MD ru-RU ru-UA rw rw-Latn-RW rw-RW rwk rwk-Latn-TZ rwk-TZ sa sa-Deva-IN sa-IN s" +
      "ah sah-Cyrl-RU sah-RU saq saq-KE saq-Latn-KE sat sat-IN sat-Olck sat-Olck-IN sbp sbp-Latn-TZ sbp-TZ " +
      "sc sc-IT sc-Latn-IT sd sd-Arab sd-Arab-PK sd-Deva sd-Deva-IN sd-IN sd-PK se se-FI se-Latn-NO se-NO s" +
      "e-SE seh seh-Latn-MZ seh-MZ ses ses-Latn-ML ses-ML sg sg-CF sg-Latn-CF shi shi-Latn shi-Latn-MA shi-" +
      "MA shi-Tfng shi-Tfng-MA si si-LK si-Sinh-LK sk sk-Latn-SK sk-SK sl sl-Latn-SI sl-SI smn smn-FI smn-L" +
      "atn-FI sms sms-FI sms-Latn-FI sn sn-Latn-ZW sn-ZW so so-DJ so-ET so-KE so-Latn-SO so-SO sq sq-AL sq-" +
      "Latn-AL sq-MK sq-XK sr sr-BA sr-CS sr-Cyrl sr-Cyrl-BA sr-Cyrl-ME sr-Cyrl-RS sr-Cyrl-XK sr-Latn sr-La" +
      "tn-BA sr-Latn-ME sr-Latn-RS sr-Latn-XK sr-ME sr-RS st st-LS st-Latn-ZA st-ZA su su-ID su-Latn su-Lat" +
      "n-ID sv sv-AX sv-FI sv-Latn-SE sv-SE sw sw-CD sw-KE sw-Latn-TZ sw-TZ sw-UG syr syr-IQ syr-SY syr-Syr" +
      "c-IQ szl szl-Latn-PL szl-PL ta ta-IN ta-LK ta-MY ta-SG ta-Taml-IN te te-IN te-Telu-IN teo teo-KE teo" +
      "-Latn-UG teo-UG tg tg-Cyrl-TJ tg-TJ th th-TH th-Thai-TH th_TH_TH ti ti-ER ti-ET ti-Ethi-ET tk tk-Lat" +
      "n-TM tk-TM tn tn-BW tn-Latn-ZA tn-ZA to to-Latn-TO to-TO tok tok-001 tok-Latn-001 tr tr-CY tr-Latn-T" +
      "R tr-TR tt tt-Cyrl-RU tt-RU twq twq-Latn-NE twq-NE tzm tzm-Latn-MA tzm-MA ug ug-Arab-CN ug-CN uk uk-" +
      "Cyrl-UA uk-UA und ur ur-Arab-PK ur-IN ur-PK uz uz-AF uz-Arab uz-Arab-AF uz-Cyrl uz-Cyrl-UZ uz-Latn u" +
      "z-Latn-UZ uz-UZ vai vai-LR vai-Latn vai-Latn-LR vai-Vaii vai-Vaii-LR vec vec-IT vec-Latn-IT vi vi-La" +
      "tn-VN vi-VN vmw vmw-Latn-MZ vmw-MZ vun vun-Latn-TZ vun-TZ wae wae-CH wae-Latn-CH wo wo-Latn-SN wo-SN" +
      " xh xh-Latn-ZA xh-ZA xnr xnr-Deva-IN xnr-IN xog xog-Latn-UG xog-UG yav yav-CM yav-Latn-CM yi yi-Hebr" +
      "-UA yi-UA yo yo-BJ yo-Latn-NG yo-NG yrl yrl-BR yrl-CO yrl-Latn-BR yrl-VE yue yue-CN yue-HK yue-Hans " +
      "yue-Hans-CN yue-Hant yue-Hant-CN yue-Hant-HK yue-Hant-MO za za-CN za-Latn-CN zgh zgh-MA zgh-Tfng-MA " +
      "zh zh-CN zh-HK zh-Hans zh-Hans-CN zh-Hans-HK zh-Hans-MO zh-Hans-MY zh-Hans-SG zh-Hant zh-Hant-HK zh-" +
      "Hant-MO zh-Hant-MY zh-Hant-TW zh-Latn zh-Latn-CN zh-MO zh-SG zh-TW zu zu-Latn-ZA zu-ZA";
}
