/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math.random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.exception.MathIllegalArgumentException;

import org.junit.Test;

public class MersenneTwisterTest {

    @Test
    public void testGaussian() {
        MersenneTwister mt = new MersenneTwister(42853252100l);
        SummaryStatistics sample = new SummaryStatistics();
        for (int i = 0; i < 1000; ++i) {
            sample.addValue(mt.nextGaussian());
        }
        assertEquals(0.0, sample.getMean(), 0.005);
        assertEquals(1.0, sample.getStandardDeviation(), 0.025);
    }

    @Test
    public void testDouble() {
        MersenneTwister mt = new MersenneTwister(195357343514l);
        SummaryStatistics sample = new SummaryStatistics();
        for (int i = 0; i < 1000; ++i) {
            sample.addValue(mt.nextDouble());
        }
        assertEquals(0.5, sample.getMean(), 0.02);
        assertEquals(1.0 / (2.0 * FastMath.sqrt(3.0)),
                     sample.getStandardDeviation(),
                     0.002);
    }

    @Test
    public void testFloat() {
        MersenneTwister mt = new MersenneTwister(4442733263l);
        SummaryStatistics sample = new SummaryStatistics();
        for (int i = 0; i < 1000; ++i) {
            sample.addValue(mt.nextFloat());
        }
        assertEquals(0.5, sample.getMean(), 0.01);
        assertEquals(1.0 / (2.0 * FastMath.sqrt(3.0)),
                     sample.getStandardDeviation(),
                     0.006);
    }

    @Test(expected=MathIllegalArgumentException.class)
    public void testNextIntNeg() {
        new MersenneTwister(1).nextInt(-1);
    }

    @Test
    public void testNextIntN() {
        MersenneTwister mt = new MersenneTwister(0x12b8a7412bb25el);
        for (int n = 1; n < 20; ++n) {
            int[] count = new int[n];
            for (int k = 0; k < 10000; ++k) {
                int l = mt.nextInt(n);
                ++count[l];
                assertTrue(l >= 0);
                assertTrue(l <  n);
            }
            for (int i = 0; i < n; ++i) {
                assertTrue(n * count[i] >  8600);
                assertTrue(n * count[i] < 11200);
            }
        }
    }

    @Test
    public void testNextInt() {
        MersenneTwister mt = new MersenneTwister(new int[] { 1, 2, 3, 4, 5 });
        int walk = 0;
        for (int k = 0; k < 10000; ++k) {
           if (mt.nextInt() >= 0) {
               ++walk;
           } else {
               --walk;
           }
        }
        assertTrue(FastMath.abs(walk) < 120);
    }

    @Test
    public void testNextLong() {
        MersenneTwister mt = new MersenneTwister(12345);
        int walk = 0;
        for (int k = 0; k < 10000; ++k) {
           if (mt.nextLong() >= 0) {
               ++walk;
           } else {
               --walk;
           }
        }
        assertTrue(FastMath.abs(walk) < 50);
    }

    @Test
    public void testNexBoolean() {
        MersenneTwister mt = new MersenneTwister(76342);
        int walk = 0;
        for (int k = 0; k < 10000; ++k) {
           if (mt.nextBoolean()) {
               ++walk;
           } else {
               --walk;
           }
        }
        assertTrue(FastMath.abs(walk) < 250);
    }

    @Test
    public void testNexBytes() {
        MersenneTwister mt = new MersenneTwister(0);
        int[] count = new int[256];
        byte[] bytes = new byte[10];
        for (int k = 0; k < 100000; ++k) {
           mt.nextBytes(bytes);
           for (byte b : bytes) {
               ++count[b + 128];
           }
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int c : count) {
            min = FastMath.min(min, c);
            max = FastMath.max(max, c);
        }
        int expected = (100000 * bytes.length) / count.length;
        assertTrue((expected - 200) < min);
        assertTrue(max < (expected + 200));
    }

    @Test
    public void testMakotoNishimura() {
        MersenneTwister mt = new MersenneTwister(new int[] {0x123, 0x234, 0x345, 0x456});
        long[] refInt = {
            1067595299l,  955945823l,  477289528l, 4107218783l, 4228976476l, 3344332714l, 3355579695l,  227628506l,
            810200273l, 2591290167l, 2560260675l, 3242736208l,  646746669l, 1479517882l, 4245472273l, 1143372638l,
            3863670494l, 3221021970l, 1773610557l, 1138697238l, 1421897700l, 1269916527l, 2859934041l, 1764463362l,
            3874892047l, 3965319921l,   72549643l, 2383988930l, 2600218693l, 3237492380l, 2792901476l,  725331109l,
            605841842l,  271258942l,  715137098l, 3297999536l, 1322965544l, 4229579109l, 1395091102l, 3735697720l,
            2101727825l, 3730287744l, 2950434330l, 1661921839l, 2895579582l, 2370511479l, 1004092106l, 2247096681l,
            2111242379l, 3237345263l, 4082424759l,  219785033l, 2454039889l, 3709582971l,  835606218l, 2411949883l,
            2735205030l,  756421180l, 2175209704l, 1873865952l, 2762534237l, 4161807854l, 3351099340l,  181129879l,
            3269891896l,  776029799l, 2218161979l, 3001745796l, 1866825872l, 2133627728l,   34862734l, 1191934573l,
            3102311354l, 2916517763l, 1012402762l, 2184831317l, 4257399449l, 2899497138l, 3818095062l, 3030756734l,
            1282161629l,  420003642l, 2326421477l, 2741455717l, 1278020671l, 3744179621l,  271777016l, 2626330018l,
            2560563991l, 3055977700l, 4233527566l, 1228397661l, 3595579322l, 1077915006l, 2395931898l, 1851927286l,
            3013683506l, 1999971931l, 3006888962l, 1049781534l, 1488758959l, 3491776230l,  104418065l, 2448267297l,
            3075614115l, 3872332600l,  891912190l, 3936547759l, 2269180963l, 2633455084l, 1047636807l, 2604612377l,
            2709305729l, 1952216715l,  207593580l, 2849898034l,  670771757l, 2210471108l,  467711165l,  263046873l,
            3569667915l, 1042291111l, 3863517079l, 1464270005l, 2758321352l, 3790799816l, 2301278724l, 3106281430l,
            7974801l, 2792461636l,  555991332l,  621766759l, 1322453093l,  853629228l,  686962251l, 1455120532l,
            957753161l, 1802033300l, 1021534190l, 3486047311l, 1902128914l, 3701138056l, 4176424663l, 1795608698l,
            560858864l, 3737752754l, 3141170998l, 1553553385l, 3367807274l,  711546358l, 2475125503l,  262969859l,
            251416325l, 2980076994l, 1806565895l,  969527843l, 3529327173l, 2736343040l, 2987196734l, 1649016367l,
            2206175811l, 3048174801l, 3662503553l, 3138851612l, 2660143804l, 1663017612l, 1816683231l,  411916003l,
            3887461314l, 2347044079l, 1015311755l, 1203592432l, 2170947766l, 2569420716l,  813872093l, 1105387678l,
            1431142475l,  220570551l, 4243632715l, 4179591855l, 2607469131l, 3090613241l,  282341803l, 1734241730l,
            1391822177l, 1001254810l,  827927915l, 1886687171l, 3935097347l, 2631788714l, 3905163266l,  110554195l,
            2447955646l, 3717202975l, 3304793075l, 3739614479l, 3059127468l,  953919171l, 2590123714l, 1132511021l,
            3795593679l, 2788030429l,  982155079l, 3472349556l,  859942552l, 2681007391l, 2299624053l,  647443547l,
            233600422l,  608168955l, 3689327453l, 1849778220l, 1608438222l, 3968158357l, 2692977776l, 2851872572l,
            246750393l, 3582818628l, 3329652309l, 4036366910l, 1012970930l,  950780808l, 3959768744l, 2538550045l,
            191422718l, 2658142375l, 3276369011l, 2927737484l, 1234200027l, 1920815603l, 3536074689l, 1535612501l,
            2184142071l, 3276955054l,  428488088l, 2378411984l, 4059769550l, 3913744741l, 2732139246l,   64369859l,
            3755670074l,  842839565l, 2819894466l, 2414718973l, 1010060670l, 1839715346l, 2410311136l,  152774329l,
            3485009480l, 4102101512l, 2852724304l,  879944024l, 1785007662l, 2748284463l, 1354768064l, 3267784736l,
            2269127717l, 3001240761l, 3179796763l,  895723219l,  865924942l, 4291570937l,   89355264l, 1471026971l,
            4114180745l, 3201939751l, 2867476999l, 2460866060l, 3603874571l, 2238880432l, 3308416168l, 2072246611l,
            2755653839l, 3773737248l, 1709066580l, 4282731467l, 2746170170l, 2832568330l,  433439009l, 3175778732l,
            26248366l, 2551382801l,  183214346l, 3893339516l, 1928168445l, 1337157619l, 3429096554l, 3275170900l,
            1782047316l, 4264403756l, 1876594403l, 4289659572l, 3223834894l, 1728705513l, 4068244734l, 2867840287l,
            1147798696l,  302879820l, 1730407747l, 1923824407l, 1180597908l, 1569786639l,  198796327l,  560793173l,
            2107345620l, 2705990316l, 3448772106l, 3678374155l,  758635715l,  884524671l,  486356516l, 1774865603l,
            3881226226l, 2635213607l, 1181121587l, 1508809820l, 3178988241l, 1594193633l, 1235154121l,  326117244l,
            2304031425l,  937054774l, 2687415945l, 3192389340l, 2003740439l, 1823766188l, 2759543402l,   10067710l,
            1533252662l, 4132494984l,   82378136l,  420615890l, 3467563163l,  541562091l, 3535949864l, 2277319197l,
            3330822853l, 3215654174l, 4113831979l, 4204996991l, 2162248333l, 3255093522l, 2219088909l, 2978279037l,
            255818579l, 2859348628l, 3097280311l, 2569721123l, 1861951120l, 2907080079l, 2719467166l,  998319094l,
            2521935127l, 2404125338l,  259456032l, 2086860995l, 1839848496l, 1893547357l, 2527997525l, 1489393124l,
            2860855349l,   76448234l, 2264934035l,  744914583l, 2586791259l, 1385380501l,   66529922l, 1819103258l,
            1899300332l, 2098173828l, 1793831094l,  276463159l,  360132945l, 4178212058l,  595015228l,  177071838l,
            2800080290l, 1573557746l, 1548998935l,  378454223l, 1460534296l, 1116274283l, 3112385063l, 3709761796l,
            827999348l, 3580042847l, 1913901014l,  614021289l, 4278528023l, 1905177404l,   45407939l, 3298183234l,
            1184848810l, 3644926330l, 3923635459l, 1627046213l, 3677876759l,  969772772l, 1160524753l, 1522441192l,
            452369933l, 1527502551l,  832490847l, 1003299676l, 1071381111l, 2891255476l,  973747308l, 4086897108l,
            1847554542l, 3895651598l, 2227820339l, 1621250941l, 2881344691l, 3583565821l, 3510404498l,  849362119l,
            862871471l,  797858058l, 2867774932l, 2821282612l, 3272403146l, 3997979905l,  209178708l, 1805135652l,
            6783381l, 2823361423l,  792580494l, 4263749770l,  776439581l, 3798193823l, 2853444094l, 2729507474l,
            1071873341l, 1329010206l, 1289336450l, 3327680758l, 2011491779l,   80157208l,  922428856l, 1158943220l,
            1667230961l, 2461022820l, 2608845159l,  387516115l, 3345351910l, 1495629111l, 4098154157l, 3156649613l,
            3525698599l, 4134908037l,  446713264l, 2137537399l, 3617403512l,  813966752l, 1157943946l, 3734692965l,
            1680301658l, 3180398473l, 3509854711l, 2228114612l, 1008102291l,  486805123l,  863791847l, 3189125290l,
            1050308116l, 3777341526l, 4291726501l,  844061465l, 1347461791l, 2826481581l,  745465012l, 2055805750l,
            4260209475l, 2386693097l, 2980646741l,  447229436l, 2077782664l, 1232942813l, 4023002732l, 1399011509l,
            3140569849l, 2579909222l, 3794857471l,  900758066l, 2887199683l, 1720257997l, 3367494931l, 2668921229l,
            955539029l, 3818726432l, 1105704962l, 3889207255l, 2277369307l, 2746484505l, 1761846513l, 2413916784l,
            2685127085l, 4240257943l, 1166726899l, 4215215715l, 3082092067l, 3960461946l, 1663304043l, 2087473241l,
            4162589986l, 2507310778l, 1579665506l,  767234210l,  970676017l,  492207530l, 1441679602l, 1314785090l,
            3262202570l, 3417091742l, 1561989210l, 3011406780l, 1146609202l, 3262321040l, 1374872171l, 1634688712l,
            1280458888l, 2230023982l,  419323804l, 3262899800l,   39783310l, 1641619040l, 1700368658l, 2207946628l,
            2571300939l, 2424079766l,  780290914l, 2715195096l, 3390957695l,  163151474l, 2309534542l, 1860018424l,
            555755123l,  280320104l, 1604831083l, 2713022383l, 1728987441l, 3639955502l,  623065489l, 3828630947l,
            4275479050l, 3516347383l, 2343951195l, 2430677756l,  635534992l, 3868699749l,  808442435l, 3070644069l,
            4282166003l, 2093181383l, 2023555632l, 1568662086l, 3422372620l, 4134522350l, 3016979543l, 3259320234l,
            2888030729l, 3185253876l, 4258779643l, 1267304371l, 1022517473l,  815943045l,  929020012l, 2995251018l,
            3371283296l, 3608029049l, 2018485115l,  122123397l, 2810669150l, 1411365618l, 1238391329l, 1186786476l,
            3155969091l, 2242941310l, 1765554882l,  279121160l, 4279838515l, 1641578514l, 3796324015l,   13351065l,
            103516986l, 1609694427l,  551411743l, 2493771609l, 1316337047l, 3932650856l, 4189700203l,  463397996l,
            2937735066l, 1855616529l, 2626847990l,   55091862l, 3823351211l,  753448970l, 4045045500l, 1274127772l,
            1124182256l,   92039808l, 2126345552l,  425973257l,  386287896l, 2589870191l, 1987762798l, 4084826973l,
            2172456685l, 3366583455l, 3602966653l, 2378803535l, 2901764433l, 3716929006l, 3710159000l, 2653449155l,
            3469742630l, 3096444476l, 3932564653l, 2595257433l,  318974657l, 3146202484l,  853571438l,  144400272l,
            3768408841l,  782634401l, 2161109003l,  570039522l, 1886241521l,   14249488l, 2230804228l, 1604941699l,
            3928713335l, 3921942509l, 2155806892l,  134366254l,  430507376l, 1924011722l,  276713377l,  196481886l,
            3614810992l, 1610021185l, 1785757066l,  851346168l, 3761148643l, 2918835642l, 3364422385l, 3012284466l,
            3735958851l, 2643153892l, 3778608231l, 1164289832l,  205853021l, 2876112231l, 3503398282l, 3078397001l,
            3472037921l, 1748894853l, 2740861475l,  316056182l, 1660426908l,  168885906l,  956005527l, 3984354789l,
            566521563l, 1001109523l, 1216710575l, 2952284757l, 3834433081l, 3842608301l, 2467352408l, 3974441264l,
            3256601745l, 1409353924l, 1329904859l, 2307560293l, 3125217879l, 3622920184l, 3832785684l, 3882365951l,
            2308537115l, 2659155028l, 1450441945l, 3532257603l, 3186324194l, 1225603425l, 1124246549l,  175808705l,
            3009142319l, 2796710159l, 3651990107l,  160762750l, 1902254979l, 1698648476l, 1134980669l,  497144426l,
            3302689335l, 4057485630l, 3603530763l, 4087252587l,  427812652l,  286876201l,  823134128l, 1627554964l,
            3745564327l, 2589226092l, 4202024494l,   62878473l, 3275585894l, 3987124064l, 2791777159l, 1916869511l,
            2585861905l, 1375038919l, 1403421920l,   60249114l, 3811870450l, 3021498009l, 2612993202l,  528933105l,
            2757361321l, 3341402964l, 2621861700l,  273128190l, 4015252178l, 3094781002l, 1621621288l, 2337611177l,
            1796718448l, 1258965619l, 4241913140l, 2138560392l, 3022190223l, 4174180924l,  450094611l, 3274724580l,
            617150026l, 2704660665l, 1469700689l, 1341616587l,  356715071l, 1188789960l, 2278869135l, 1766569160l,
            2795896635l,   57824704l, 2893496380l, 1235723989l, 1630694347l, 3927960522l,  428891364l, 1814070806l,
            2287999787l, 4125941184l, 3968103889l, 3548724050l, 1025597707l, 1404281500l, 2002212197l,   92429143l,
            2313943944l, 2403086080l, 3006180634l, 3561981764l, 1671860914l, 1768520622l, 1803542985l,  844848113l,
            3006139921l, 1410888995l, 1157749833l, 2125704913l, 1789979528l, 1799263423l,  741157179l, 2405862309l,
            767040434l, 2655241390l, 3663420179l, 2172009096l, 2511931187l, 1680542666l,  231857466l, 1154981000l,
            157168255l, 1454112128l, 3505872099l, 1929775046l, 2309422350l, 2143329496l, 2960716902l,  407610648l,
            2938108129l, 2581749599l,  538837155l, 2342628867l,  430543915l,  740188568l, 1937713272l, 3315215132l,
            2085587024l, 4030765687l,  766054429l, 3517641839l,  689721775l, 1294158986l, 1753287754l, 4202601348l,
            1974852792l,   33459103l, 3568087535l, 3144677435l, 1686130825l, 4134943013l, 3005738435l, 3599293386l,
            426570142l,  754104406l, 3660892564l, 1964545167l,  829466833l,  821587464l, 1746693036l, 1006492428l,
            1595312919l, 1256599985l, 1024482560l, 1897312280l, 2902903201l,  691790057l, 1037515867l, 3176831208l,
            1968401055l, 2173506824l, 1089055278l, 1748401123l, 2941380082l,  968412354l, 1818753861l, 2973200866l,
            3875951774l, 1119354008l, 3988604139l, 1647155589l, 2232450826l, 3486058011l, 3655784043l, 3759258462l,
            847163678l, 1082052057l,  989516446l, 2871541755l, 3196311070l, 3929963078l,  658187585l, 3664944641l,
            2175149170l, 2203709147l, 2756014689l, 2456473919l, 3890267390l, 1293787864l, 2830347984l, 3059280931l,
            4158802520l, 1561677400l, 2586570938l,  783570352l, 1355506163l,   31495586l, 3789437343l, 3340549429l,
            2092501630l,  896419368l,  671715824l, 3530450081l, 3603554138l, 1055991716l, 3442308219l, 1499434728l,
            3130288473l, 3639507000l,   17769680l, 2259741420l,  487032199l, 4227143402l, 3693771256l, 1880482820l,
            3924810796l,  381462353l, 4017855991l, 2452034943l, 2736680833l, 2209866385l, 2128986379l,  437874044l,
            595759426l,  641721026l, 1636065708l, 3899136933l,  629879088l, 3591174506l,  351984326l, 2638783544l,
            2348444281l, 2341604660l, 2123933692l,  143443325l, 1525942256l,  364660499l,  599149312l,  939093251l,
            1523003209l,  106601097l,  376589484l, 1346282236l, 1297387043l,  764598052l, 3741218111l,  933457002l,
            1886424424l, 3219631016l,  525405256l, 3014235619l,  323149677l, 2038881721l, 4100129043l, 2851715101l,
            2984028078l, 1888574695l, 2014194741l, 3515193880l, 4180573530l, 3461824363l, 2641995497l, 3179230245l,
            2902294983l, 2217320456l, 4040852155l, 1784656905l, 3311906931l,   87498458l, 2752971818l, 2635474297l,
            2831215366l, 3682231106l, 2920043893l, 3772929704l, 2816374944l,  309949752l, 2383758854l,  154870719l,
            385111597l, 1191604312l, 1840700563l,  872191186l, 2925548701l, 1310412747l, 2102066999l, 1504727249l,
            3574298750l, 1191230036l, 3330575266l, 3180292097l, 3539347721l,  681369118l, 3305125752l, 3648233597l,
            950049240l, 4173257693l, 1760124957l,  512151405l,  681175196l,  580563018l, 1169662867l, 4015033554l,
            2687781101l,  699691603l, 2673494188l, 1137221356l,  123599888l,  472658308l, 1053598179l, 1012713758l,
            3481064843l, 3759461013l, 3981457956l, 3830587662l, 1877191791l, 3650996736l,  988064871l, 3515461600l,
            4089077232l, 2225147448l, 1249609188l, 2643151863l, 3896204135l, 2416995901l, 1397735321l, 3460025646l
        };
        double[] refDouble = {
            0.76275443, 0.99000644, 0.98670464, 0.10143112, 0.27933125, 0.69867227, 0.94218740, 0.03427201,
            0.78842173, 0.28180608, 0.92179002, 0.20785655, 0.54534773, 0.69644020, 0.38107718, 0.23978165,
            0.65286910, 0.07514568, 0.22765211, 0.94872929, 0.74557914, 0.62664415, 0.54708246, 0.90959343,
            0.42043116, 0.86334511, 0.19189126, 0.14718544, 0.70259889, 0.63426346, 0.77408121, 0.04531601,
            0.04605807, 0.88595519, 0.69398270, 0.05377184, 0.61711170, 0.05565708, 0.10133577, 0.41500776,
            0.91810699, 0.22320679, 0.23353705, 0.92871862, 0.98897234, 0.19786706, 0.80558809, 0.06961067,
            0.55840445, 0.90479405, 0.63288060, 0.95009721, 0.54948447, 0.20645042, 0.45000959, 0.87050869,
            0.70806991, 0.19406895, 0.79286390, 0.49332866, 0.78483914, 0.75145146, 0.12341941, 0.42030252,
            0.16728160, 0.59906494, 0.37575460, 0.97815160, 0.39815952, 0.43595080, 0.04952478, 0.33917805,
            0.76509902, 0.61034321, 0.90654701, 0.92915732, 0.85365931, 0.18812377, 0.65913428, 0.28814566,
            0.59476081, 0.27835931, 0.60722542, 0.68310435, 0.69387186, 0.03699800, 0.65897714, 0.17527003,
            0.02889304, 0.86777366, 0.12352068, 0.91439461, 0.32022990, 0.44445731, 0.34903686, 0.74639273,
            0.65918367, 0.92492794, 0.31872642, 0.77749724, 0.85413832, 0.76385624, 0.32744211, 0.91326300,
            0.27458185, 0.22190155, 0.19865383, 0.31227402, 0.85321225, 0.84243342, 0.78544200, 0.71854080,
            0.92503892, 0.82703064, 0.88306297, 0.47284073, 0.70059042, 0.48003761, 0.38671694, 0.60465770,
            0.41747204, 0.47163243, 0.72750808, 0.65830223, 0.10955369, 0.64215401, 0.23456345, 0.95944940,
            0.72822249, 0.40888451, 0.69980355, 0.26677428, 0.57333635, 0.39791582, 0.85377858, 0.76962816,
            0.72004885, 0.90903087, 0.51376506, 0.37732665, 0.12691640, 0.71249738, 0.81217908, 0.37037313,
            0.32772374, 0.14238259, 0.05614811, 0.74363008, 0.39773267, 0.94859135, 0.31452454, 0.11730313,
            0.62962618, 0.33334237, 0.45547255, 0.10089665, 0.56550662, 0.60539371, 0.16027624, 0.13245301,
            0.60959939, 0.04671662, 0.99356286, 0.57660859, 0.40269560, 0.45274629, 0.06699735, 0.85064246,
            0.87742744, 0.54508392, 0.87242982, 0.29321385, 0.67660627, 0.68230715, 0.79052073, 0.48592054,
            0.25186266, 0.93769755, 0.28565487, 0.47219067, 0.99054882, 0.13155240, 0.47110470, 0.98556600,
            0.84397623, 0.12875246, 0.90953202, 0.49129015, 0.23792727, 0.79481194, 0.44337770, 0.96564297,
            0.67749118, 0.55684872, 0.27286897, 0.79538393, 0.61965356, 0.22487929, 0.02226018, 0.49248200,
            0.42247006, 0.91797788, 0.99250134, 0.23449967, 0.52531508, 0.10246337, 0.78685622, 0.34310922,
            0.89892996, 0.40454552, 0.68608407, 0.30752487, 0.83601319, 0.54956031, 0.63777550, 0.82199797,
            0.24890696, 0.48801123, 0.48661910, 0.51223987, 0.32969635, 0.31075073, 0.21393155, 0.73453207,
            0.15565705, 0.58584522, 0.28976728, 0.97621478, 0.61498701, 0.23891470, 0.28518540, 0.46809591,
            0.18371914, 0.37597910, 0.13492176, 0.66849449, 0.82811466, 0.56240330, 0.37548956, 0.27562998,
            0.27521910, 0.74096121, 0.77176757, 0.13748143, 0.99747138, 0.92504502, 0.09175241, 0.21389176,
            0.21766512, 0.31183245, 0.23271221, 0.21207367, 0.57903312, 0.77523344, 0.13242613, 0.31037988,
            0.01204835, 0.71652949, 0.84487594, 0.14982178, 0.57423142, 0.45677888, 0.48420169, 0.53465428,
            0.52667473, 0.46880526, 0.49849733, 0.05670710, 0.79022476, 0.03872047, 0.21697212, 0.20443086,
            0.28949326, 0.81678186, 0.87629474, 0.92297064, 0.27373097, 0.84625273, 0.51505586, 0.00582792,
            0.33295971, 0.91848412, 0.92537226, 0.91760033, 0.07541125, 0.71745848, 0.61158698, 0.00941650,
            0.03135554, 0.71527471, 0.24821915, 0.63636652, 0.86159918, 0.26450229, 0.60160194, 0.35557725,
            0.24477500, 0.07186456, 0.51757096, 0.62120362, 0.97981062, 0.69954667, 0.21065616, 0.13382753,
            0.27693186, 0.59644095, 0.71500764, 0.04110751, 0.95730081, 0.91600724, 0.47704678, 0.26183479,
            0.34706971, 0.07545431, 0.29398385, 0.93236070, 0.60486023, 0.48015011, 0.08870451, 0.45548581,
            0.91872718, 0.38142712, 0.10668643, 0.01397541, 0.04520355, 0.93822273, 0.18011940, 0.57577277,
            0.91427606, 0.30911399, 0.95853475, 0.23611214, 0.69619891, 0.69601980, 0.76765372, 0.58515930,
            0.49479057, 0.11288752, 0.97187699, 0.32095365, 0.57563608, 0.40760618, 0.78703383, 0.43261152,
            0.90877651, 0.84686346, 0.10599030, 0.72872803, 0.19315490, 0.66152912, 0.10210518, 0.06257876,
            0.47950688, 0.47062066, 0.72701157, 0.48915116, 0.66110261, 0.60170685, 0.24516994, 0.12726050,
            0.03451185, 0.90864994, 0.83494878, 0.94800035, 0.91035206, 0.14480751, 0.88458997, 0.53498312,
            0.15963215, 0.55378627, 0.35171349, 0.28719791, 0.09097957, 0.00667896, 0.32309622, 0.87561479,
            0.42534520, 0.91748977, 0.73908457, 0.41793223, 0.99279792, 0.87908370, 0.28458072, 0.59132853,
            0.98672190, 0.28547393, 0.09452165, 0.89910674, 0.53681109, 0.37931425, 0.62683489, 0.56609740,
            0.24801549, 0.52948179, 0.98328855, 0.66403523, 0.55523786, 0.75886666, 0.84784685, 0.86829981,
            0.71448906, 0.84670080, 0.43922919, 0.20771016, 0.64157936, 0.25664246, 0.73055695, 0.86395782,
            0.65852932, 0.99061803, 0.40280575, 0.39146298, 0.07291005, 0.97200603, 0.20555729, 0.59616495,
            0.08138254, 0.45796388, 0.33681125, 0.33989127, 0.18717090, 0.53545811, 0.60550838, 0.86520709,
            0.34290701, 0.72743276, 0.73023855, 0.34195926, 0.65019733, 0.02765254, 0.72575740, 0.32709576,
            0.03420866, 0.26061893, 0.56997511, 0.28439072, 0.84422744, 0.77637570, 0.55982168, 0.06720327,
            0.58449067, 0.71657369, 0.15819609, 0.58042821, 0.07947911, 0.40193792, 0.11376012, 0.88762938,
            0.67532159, 0.71223735, 0.27829114, 0.04806073, 0.21144026, 0.58830274, 0.04140071, 0.43215628,
            0.12952729, 0.94668759, 0.87391019, 0.98382450, 0.27750768, 0.90849647, 0.90962737, 0.59269720,
            0.96102026, 0.49544979, 0.32007095, 0.62585546, 0.03119821, 0.85953001, 0.22017528, 0.05834068,
            0.80731217, 0.53799961, 0.74166948, 0.77426600, 0.43938444, 0.54862081, 0.58575513, 0.15886492,
            0.73214332, 0.11649057, 0.77463977, 0.85788827, 0.17061997, 0.66838056, 0.96076133, 0.07949296,
            0.68521946, 0.89986254, 0.05667410, 0.12741385, 0.83470977, 0.63969104, 0.46612929, 0.10200126,
            0.01194925, 0.10476340, 0.90285217, 0.31221221, 0.32980614, 0.46041971, 0.52024973, 0.05425470,
            0.28330912, 0.60426543, 0.00598243, 0.97244013, 0.21135841, 0.78561597, 0.78428734, 0.63422849,
            0.32909934, 0.44771136, 0.27380750, 0.14966697, 0.18156268, 0.65686758, 0.28726350, 0.97074787,
            0.63676171, 0.96649494, 0.24526295, 0.08297372, 0.54257548, 0.03166785, 0.33735355, 0.15946671,
            0.02102971, 0.46228045, 0.11892296, 0.33408336, 0.29875681, 0.29847692, 0.73767569, 0.02080745,
            0.62980060, 0.08082293, 0.22993106, 0.25031439, 0.87787525, 0.45150053, 0.13673441, 0.63407612,
            0.97907688, 0.52241942, 0.50580158, 0.06273902, 0.05270283, 0.77031811, 0.05113352, 0.24393329,
            0.75036441, 0.37436336, 0.22877652, 0.59975358, 0.85707591, 0.88691457, 0.85547165, 0.36641027,
            0.58720133, 0.45462835, 0.09243817, 0.32981586, 0.07820411, 0.25421519, 0.36004706, 0.60092307,
            0.46192412, 0.36758683, 0.98424170, 0.08019934, 0.68594024, 0.45826386, 0.29962317, 0.79365413,
            0.89231296, 0.49478547, 0.87645944, 0.23590734, 0.28106737, 0.75026285, 0.08136314, 0.79582424,
            0.76010628, 0.82792971, 0.27947652, 0.72482861, 0.82191216, 0.46171689, 0.79189752, 0.96043686,
            0.51609668, 0.88995725, 0.28998963, 0.55191845, 0.03934737, 0.83033700, 0.49553013, 0.98009549,
            0.19017594, 0.98347750, 0.33452066, 0.87144372, 0.72106301, 0.71272114, 0.71465963, 0.88361677,
            0.85571283, 0.73782329, 0.20920458, 0.34855153, 0.46766817, 0.02780062, 0.74898344, 0.03680650,
            0.44866557, 0.77426312, 0.91025891, 0.25195236, 0.87319953, 0.63265037, 0.25552148, 0.27422476,
            0.95217406, 0.39281839, 0.66441573, 0.09158900, 0.94515992, 0.07800798, 0.02507888, 0.39901462,
            0.17382573, 0.12141278, 0.85502334, 0.19902911, 0.02160210, 0.44460522, 0.14688742, 0.68020336,
            0.71323733, 0.60922473, 0.95400380, 0.99611159, 0.90897777, 0.41073520, 0.66206647, 0.32064685,
            0.62805003, 0.50677209, 0.52690101, 0.87473387, 0.73918362, 0.39826974, 0.43683919, 0.80459118,
            0.32422684, 0.01958019, 0.95319576, 0.98326137, 0.83931735, 0.69060863, 0.33671416, 0.68062550,
            0.65152380, 0.33392969, 0.03451730, 0.95227244, 0.68200635, 0.85074171, 0.64721009, 0.51234433,
            0.73402047, 0.00969637, 0.93835057, 0.80803854, 0.31485260, 0.20089527, 0.01323282, 0.59933780,
            0.31584602, 0.20209563, 0.33754800, 0.68604181, 0.24443049, 0.19952227, 0.78162632, 0.10336988,
            0.11360736, 0.23536740, 0.23262256, 0.67803776, 0.48749791, 0.74658435, 0.92156640, 0.56706407,
            0.36683221, 0.99157136, 0.23421374, 0.45183767, 0.91609720, 0.85573315, 0.37706276, 0.77042618,
            0.30891908, 0.40709595, 0.06944866, 0.61342849, 0.88817388, 0.58734506, 0.98711323, 0.14744128,
            0.63242656, 0.87704136, 0.68347125, 0.84446569, 0.43265239, 0.25146321, 0.04130111, 0.34259839,
            0.92697368, 0.40878778, 0.56990338, 0.76204273, 0.19820348, 0.66314909, 0.02482844, 0.06669207,
            0.50205581, 0.26084093, 0.65139159, 0.41650223, 0.09733904, 0.56344203, 0.62651696, 0.67332139,
            0.58037374, 0.47258086, 0.21010758, 0.05713135, 0.89390629, 0.10781246, 0.32037450, 0.07628388,
            0.34227964, 0.42190597, 0.58201860, 0.77363549, 0.49595133, 0.86031236, 0.83906769, 0.81098161,
            0.26694195, 0.14215941, 0.88210306, 0.53634237, 0.12090720, 0.82480459, 0.75930318, 0.31847147,
            0.92768077, 0.01037616, 0.56201727, 0.88107122, 0.35925856, 0.85860762, 0.61109408, 0.70408301,
            0.58434977, 0.92192494, 0.62667915, 0.75988365, 0.06858761, 0.36156496, 0.58057195, 0.13636150,
            0.57719713, 0.59340255, 0.63530602, 0.22976282, 0.71915530, 0.41162531, 0.63979565, 0.09931342,
            0.79344045, 0.10893790, 0.84450224, 0.23122236, 0.99485593, 0.73637397, 0.17276368, 0.13357764,
            0.74965804, 0.64991737, 0.61990341, 0.41523170, 0.05878239, 0.05687301, 0.05497131, 0.42868366,
            0.42571090, 0.25810502, 0.89642955, 0.30439758, 0.39310223, 0.11357431, 0.04288255, 0.23397550,
            0.11200634, 0.85621396, 0.89733974, 0.37508865, 0.42077265, 0.68597384, 0.72781399, 0.19296476,
            0.61699087, 0.31667128, 0.67756410, 0.00177323, 0.05725176, 0.79474693, 0.18885238, 0.06724856,
            0.68193156, 0.42202167, 0.22082041, 0.28554673, 0.64995708, 0.87851940, 0.29124547, 0.61009521,
            0.87374537, 0.05743712, 0.69902994, 0.81925115, 0.45653873, 0.37236821, 0.31118709, 0.52734307,
            0.39672836, 0.38185294, 0.30163915, 0.17374510, 0.04913278, 0.90404879, 0.25742801, 0.58266467,
            0.97663209, 0.79823377, 0.36437958, 0.15206043, 0.26529938, 0.22690047, 0.05839021, 0.84721160,
            0.18622435, 0.37809403, 0.55706977, 0.49828704, 0.47659049, 0.24289680, 0.88477595, 0.07807463,
            0.56245739, 0.73490635, 0.21099431, 0.13164942, 0.75840044, 0.66877037, 0.28988183, 0.44046090,
            0.24967434, 0.80048356, 0.26029740, 0.30416821, 0.64151867, 0.52067892, 0.12880774, 0.85465381,
            0.02690525, 0.19149288, 0.49630295, 0.79682619, 0.43566145, 0.00288078, 0.81484193, 0.03763639,
            0.68529083, 0.01339574, 0.38405386, 0.30537067, 0.22994703, 0.44000045, 0.27217985, 0.53831243,
            0.02870435, 0.86282045, 0.61831306, 0.09164956, 0.25609707, 0.07445781, 0.72185784, 0.90058883,
            0.30070608, 0.94476583, 0.56822213, 0.21933909, 0.96772793, 0.80063440, 0.26307906, 0.31183306,
            0.16501252, 0.55436179, 0.68562285, 0.23829083, 0.86511559, 0.57868991, 0.81888344, 0.20126869,
            0.93172350, 0.66028129, 0.21786948, 0.78515828, 0.10262106, 0.35390326, 0.79303876, 0.63427924,
            0.90479631, 0.31024934, 0.60635447, 0.56198079, 0.63573813, 0.91854197, 0.99701497, 0.83085849,
            0.31692291, 0.01925964, 0.97446405, 0.98751283, 0.60944293, 0.13751018, 0.69519957, 0.68956636,
            0.56969015, 0.46440193, 0.88341765, 0.36754434, 0.89223647, 0.39786427, 0.85055280, 0.12749961,
            0.79452122, 0.89449784, 0.14567830, 0.45716830, 0.74822309, 0.28200437, 0.42546044, 0.17464886,
            0.68308746, 0.65496587, 0.52935411, 0.12736159, 0.61523955, 0.81590528, 0.63107864, 0.39786553,
            0.20102294, 0.53292914, 0.75485590, 0.59847044, 0.32861691, 0.12125866, 0.58917183, 0.07638293,
            0.86845380, 0.29192617, 0.03989733, 0.52180460, 0.32503407, 0.64071852, 0.69516575, 0.74254998,
            0.54587026, 0.48713246, 0.32920155, 0.08719954, 0.63497059, 0.54328459, 0.64178757, 0.45583809,
            0.70694291, 0.85212760, 0.86074305, 0.33163422, 0.85739792, 0.59908488, 0.74566046, 0.72157152
        };

        for (int i = 0; i < refInt.length; ++i) {
            int r = mt.nextInt();
            assertEquals(refInt[i], (r & 0x7fffffffl) | ((r < 0) ? 0x80000000l : 0x0l));
        }

        for (int i = 0; i < refDouble.length; ++i) {
            int r = mt.nextInt();
            assertEquals(refDouble[i],
                         ((r & 0x7fffffffl) | ((r < 0) ? 0x80000000l : 0x0l)) / 4294967296.0,
                         1.0e-8);
        }

    }

}
