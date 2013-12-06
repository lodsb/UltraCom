/*
  +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013-11-07 :: 17:46
    >>  Origin: 
    >>
  +3>>
    >>  Copyright (c) 2013:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas Klügel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 */

package PitchIt

object ScaleLib {
  private lazy val ids2Names = generateEquivalenceClasses(12) ++ Map[Int, String](
    (3926	->	"Adonai Malakh (Israel)"),
    (2477	->	"Aeolian Flat 1"),
    (2937	->	"Algerian"),
    (3156	->	"Altered Pentatonic"),
    (3456	->	"Alternating Tetramirror"),
    (2772	->	"Arezzo Major Diatonic Hexachord"),
    (2184	->	"Augmented Chord"),
    (3248	->	"Balinese Pentachord"),
    (3285	->	"Bhairubahar Thaat (India)"),
    (2322	->	"Bi Yu (China)"),
    (2418	->	"Blues"),
    (3510	->	"Blues Diminished"),
    (2964	->	"Blues Dorian Hexatonic"),
    (3062	->	"Blues Enneatonic"),
    (2422	->	"Blues Heptatonic"),
    (2930	->	"Blues Modified"),
    (2934	->	"Blues Octatonic"),
    (3872	->	"Blues Pentacluster"),
    (2386	->	"Blues Pentatonic"),
    (3442	->	"Blues Phrygian"),
    (2419	->	"Blues With Leading Tone"),
    (2504	->	"Center-Cluster Pentamirror"),
    (2896	->	"Chad Gadyo (Israel)"),
    (2634	->	"Chaio (China)"),
    (3799	->	"Chromatic Bebop"),
    (4092	->	"Chromatic Decamirror"),
    (3934	->	"Chromatic Diatonic Dorian"),
    (3676	->	"Chromatic Dorian"),
    (2515	->	"Chromatic Dorian Inverse"),
    (4064	->	"Chromatic Heptamirror"),
    (4032	->	"Chromatic Hexamirror"),
    (2972	->	"Chromatic Hypodorian"),
    (2510	->	"Chromatic Hypodorian Inverse"),
    (3257	->	"Chromatic Hypolydian"),
    (3305	->	"Chromatic Hypolydian Inverse"),
    (3700	->	"Chromatic Hypophrygian Inverse"),
    (3301	->	"Chromatic Lydian"),
    (3385	->	"Chromatic Lydian Inverse"),
    (3698	->	"Chromatic Mixolydian"),
    (2675	->	"Chromatic Mixolydian Inverse"),
    (4088	->	"Chromatic Nonamirror"),
    (4080	->	"Chromatic Octamirror"),
    (3968	->	"Chromatic Pentamirror"),
    (3805	->	"Chromatic Permuted Diatonic Dorian"),
    (2507	->	"Chromatic Phrygian"),
    (3740	->	"Chromatic Phrygian Inverse"),
    (3840	->	"Chromatic Tetramirror"),
    (3584	->	"Chromatic Trimirror"),
    (4094	->	"Chromatic Undecamirror"),
    (2336	->	"Diminished Chord"),
    (2925	->	"Diminished Scale"),
    (2340	->	"Diminished Seventh Chord"),
    (2775	->	"Dominant Bebop"),
    (2706	->	"Dominant Pentatonic"),
    (2902	->	"Dorian"),
    (2910	->	"Dorian Aeolian"),
    (2918	->	"Dorian Flat 5"),
    (2836	->	"Dorian Pentatonic"),
    (3392	->	"Dorian Tetrachord"),
    (3290	->	"Dorico Flamenco"),
    (3428	->	"Double-Phrygian Hexatonic"),
    (2729	->	"Eskimo Hexatonic 2 (North America)"),
    (2704	->	"Eskimo Tetratonic (North America)"),
    (2911	->	"Full Minor"),
    (3549	->	"Genus Chromaticum"),
    (2805	->	"Genus Diatonicum Veterum Correctum"),
    (2640	->	"Genus Primum"),
    (2130	->	"Genus Primum Inverse"),
    (2261	->	"Genus Secundum"),
    (3292	->	"Gipsy Hexatonic"),
    (2870	->	"Gnossiennes"),
    (3449	->	"Half-Diminished Bebop"),
    (2648	->	"Han-kumoi (Japan)"),
    (2777	->	"Harmonic Major"),
    (2905	->	"Harmonic Minor"),
    (3286	->	"Harmonic Minor Inverse"),
    (2848	->	"Harmonic Minor Tetrachord"),
    (3929	->	"Harmonic Neapolitan Minor"),
    (2837	->	"Hawaiian"),
    (2840	->	"Hira-joshi (Japan)"),
    (3426	->	"Honchoshi Plagal Form (Japan)"),
    (3038	->	"Houseini (Greece)"),
    (2517	->	"Houzam (Greece)"),
    (2486	->	"Hungarian Major"),
    (2765	->	"Ionian Sharp 5"),
    (3170	->	"Iwato (Japan)"),
    (3376	->	"Javanese Pentachord"),
    (2901	->	"Jazz Minor"),
    (3414	->	"Jazz Minor Inverse"),
    (2942	->	"Kiourdi (Greece)"),
    (3154	->	"Kokin-joshi, Miyakobushi (Japan)"),
    (2724	->	"Kung (China)"),
    (3434	->	"Locrian"),
    (2921	->	"Locrian 2"),
    (3436	->	"Locrian Double-Flat 7"),
    (3430	->	"Locrian Natural 6"),
    (3424	->	"Locrian Pentamirror"),
    (2741	->	"Lydian"),
    (2733	->	"Lydian Augmented"),
    (2869	->	"Lydian Diminished"),
    (2709	->	"Lydian Hexatonic"),
    (2746	->	"Lydian Minor"),
    (2736	->	"Lydian Pentachord"),
    (2485	->	"Lydian Sharp 2"),
    (2453	->	"Lydian Sharp 2 Hexatonic"),
    (3501	->	"Magen Abot (Israel)"),
    (2773	->	"Major"),
    (2781	->	"Major Bebop"),
    (2716	->	"Major Bebop Hexatonic"),
    (2192	->	"Major Chord"),
    (3289	->	"Major Gipsy"),
    (2794	->	"Major Locrian"),
    (2778	->	"Major Minor"),
    (3039	->	"Major and Minor Mixed"),
    (2768	->	"Major Pentachord"),
    (2708	->	"Major Pentatonic"),
    (2049	->	"Major Seventh Interval"),
    (2052	->	"Major Sixth Interval"),
    (2752	->	"Major Tetrachord"),
    (2176	->	"Major Third Interval"),
    (3291	->	"Maqam Hijaz (Arabia)"),
    (3558	->	"Maqam Shadd'araban (Arabia)"),
    (3253	->	"Marva Thaat (India)"),
    (3386	->	"Mela Bhavapriya (India)"),
    (2739	->	"Mela Citrambari (India)"),
    (2489	->	"Mela Dhatuvardhani (India)"),
    (3260	->	"Mela Dhavalambari (India)"),
    (3379	->	"Mela Divyamani (India)"),
    (3673	->	"Mela Ganamurti (India)"),
    (2521	->	"Mela Gangeyabhusani (India)"),
    (3388	->	"Mela Gavambodhi (India)"),
    (3283	->	"Mela Hatakambari (India)"),
    (3642	->	"Mela Jalarnava (India)"),
    (3641	->	"Mela Jhalavarali (India)"),
    (2908	->	"Mela Jhankaradhvani (India)"),
    (2490	->	"Mela Jyotisvarupini (India)"),
    (2748	->	"Mela Kantamani (India)"),
    (2745	->	"Mela Latangi (India)"),
    (3669	->	"Mela Manavati (India)"),
    (2780	->	"Mela Mararanjani (India)"),
    (2771	->	"Mela Naganandini (India)"),
    (3258	->	"Mela Namanarayani (India)"),
    (3638	->	"Mela Navanitam (India)"),
    (2867	->	"Mela Nitimati (India)"),
    (3637	->	"Mela Pavani (India)"),
    (2522	->	"Mela Ragavardhani (India)"),
    (3635	->	"Mela Raghupriya (India)"),
    (2483	->	"Mela Rasikapriya (India)"),
    (3674	->	"Mela Ratnangi (India)"),
    (3411	->	"Mela Rupavati (India)"),
    (3382	->	"Mela Sadvidhamargini (India)"),
    (3644	->	"Mela Salaga (India)"),
    (2874	->	"Mela Sanmukhapriya (India)"),
    (3420	->	"Mela Senavati (India)"),
    (2492	->	"Mela Sucaritra (India)"),
    (3381	->	"Mela Suvarnangi (India)"),
    (2876	->	"Mela Syamalangi (India)"),
    (3667	->	"Mela Tanarupi (India)"),
    (3670	->	"Mela Vanaspati (India)"),
    (2899	->	"Mela Varunapriya (India)"),
    (3251	->	"Mela Visvambhari (India)"),
    (2524	->	"Mela Yagapriya (India)"),
    (3822	->	"Messiaen Mode 3"),
    (3003	->	"Messiaen Mode 3 Inverse"),
    (3900	->	"Messiaen Mode 4"),
    (2535	->	"Messiaen Mode 4 Inverse"),
    (3640	->	"Messiaen Mode 5"),
    (2275	->	"Messiaen Mode 5 Inverse"),
    (3770	->	"Messiaen Mode 6"),
    (2795	->	"Messiaen Mode 6 Inverse"),
    (4030	->	"Messiaen Mode 7"),
    (3055	->	"Messiaen Mode 7 Inverse"),
    (3380	->	"Messiaen Truncated Mode 2"),
    (3276	->	"Messiaen Truncated Mode 3"),
    (2457	->	"Messiaen Truncated Mode 3 Inverse"),
    (3120	->	"Messiaen Truncated Mode 5"),
    (2145	->	"Messiaen Truncated Mode 5 Inverse"),
    (2600	->	"Messiaen Truncated Mode 6"),
    (2210	->	"Messiaen Truncated Mode 6 Inverse"),
    (2906	->	"Minor"),
    (2388	->	"Minor Added Sixth Pentatonic"),
    (3030	->	"Minor Bebop"),
    (2966	->	"Minor Bebop Hexatonic"),
    (2320	->	"Minor Chord"),
    (2873	->	"Minor Gipsy"),
    (2898	->	"Minor Hexatonic"),
    (2922	->	"Minor Locrian"),
    (3063	->	"Minor Pentatonic With Leading Tones"),
    (2050	->	"Minor Seventh Interval"),
    (2056	->	"Minor Sixth Interval"),
    (2304	->	"Minor Third Interval"),
    (2816	->	"Minor Trichord"),
    (2774	->	"Mixolydian"),
    (2790	->	"Mixolydian Flat 5"),
    (2646	->	"Mixolydian Hexatonic"),
    (2258	->	"Mixolydian Pentatonic"),
    (2766	->	"Mixolydian Sharp 5"),
    (3547	->	"Moorish Phrygian"),
    (3413	->	"Neapolitan Major"),
    (3417	->	"Neapolitan Minor"),
    (3756	->	"Neapolitan Minor Mode"),
    (3387	->	"Neveseri (Greece)"),
    (2669	->	"Nohkan (Japan)"),
    (3302	->	"Oriental"),
    (3680	->	"Oriental Pentacluster"),
    (2742	->	"Overtone"),
    (3352	->	"Pelog (Bali)"),
    (2064	->	"Perfect Fifth Interval"),
    (2112	->	"Perfect Forth Interval"),
    (3418	->	"Phrygian"),
    (3930	->	"Phrygian Aeolian"),
    (3482	->	"Phrygian Flat 4"),
    (2394	->	"Phrygian Hexatonic"),
    (3450	->	"Phrygian Locrian"),
    (2880	->	"Phrygian Tetrachord"),
    (3328	->	"Phrygian Trichord"),
    (3435	->	"Prokofiev Scale"),
    (2726	->	"Prometheus"),
    (3238	->	"Prometheus Neapolitan"),
    (2916	->	"Pyramid Hexatonic"),
    (2884	->	"Raga Abhogi (India)"),
    (2865	->	"Raga Amarasenapriya (India)"),
    (2888	->	"Raga Audav Tukhari (India)"),
    (2886	->	"Raga Bagesri, Sriranjani, Kapijingla (India)"),
    (3225	->	"Raga Bauli (India)"),
    (2514	->	"Raga Bhanumanjari (India)"),
    (3317	->	"Raga Bhatiyar (India)"),
    (2628	->	"Raga Bhavani (India)"),
    (3370	->	"Raga Bhavani (India)"),
    (2649	->	"Raga Bhinna Pancama (India)"),
    (2245	->	"Raga Bhinna Shadja, Hindolita (India)"),
    (2712	->	"Raga Bhupeshwari, Janasammodini (India)"),
    (2180	->	"Raga Bilwadala (India)"),
    (2737	->	"Raga Caturangini (India)"),
    (3636	->	"Raga Chandrajyoti (India)"),
    (2374	->	"Raga Chandrakauns Kafi, Surya (India)"),
    (2377	->	"Raga Chandrakauns Kiravani (India)"),
    (2373	->	"Raga Chandrakauns Modern, Marga Hindola (India)"),
    (3368	->	"Raga Chhaya Todi (India)"),
    (3400	->	"Raga Chitthakarshini (India)"),
    (2878	->	"Raga Cintamani (India)"),
    (3097	->	"Raga Deshgaur (India)"),
    (2641	->	"Raga Desh (India)"),
    (2137	->	"Raga Devaranjani (India)"),
    (3256	->	"Raga Dhavalangam (India)"),
    (2228	->	"Raga Dhavalashri (India)"),
    (2800	->	"Raga Dipak (India)"),
    (2257	->	"Raga Gambhiranata (India)"),
    (3410	->	"Raga Gandharavam (India)"),
    (3281	->	"Raga Gaula (India)"),
    (3153	->	"Raga Gauri (India)"),
    (2889	->	"Raga Ghantana (India)"),
    (2630	->	"Raga Guhamanohari (India)"),
    (3369	->	"Raga Gurjari Todi (India)"),
    (2705	->	"Raga Hamsadhvani (India)"),
    (3237	->	"Raga Hamsanandi, Puriya (India)"),
    (2757	->	"Raga Hamsa Vinodini (India)"),
    (2346	->	"Raga Harikauns (India)"),
    (2632	->	"Raga Haripriya (India)"),
    (3244	->	"Raga Hejjajji (India)"),
    (2213	->	"Raga Hindol (India)"),
    (3250	->	"Raga Indupriya (India)"),
    (2618	->	"Raga Jaganmohanam (India)"),
    (2402	->	"Raga Jayakauns (India)"),
    (3157	->	"Raga Jivantika (India)"),
    (2355	->	"Raga Jivantini, Gaurikriya (India)"),
    (2234	->	"Raga Jyoti (India)"),
    (3228	->	"Raga Kalagada (India)"),
    (3164	->	"Raga Kalakanthi (India)"),
    (3284	->	"Raga Kalavati, Ragamalini (India)"),
    (2266	->	"Raga Kamalamanohari (India)"),
    (3354	->	"Raga Kashyapi (India)"),
    (2246	->	"Raga Khamaji Durga (India)"),
    (2262	->	"Raga Khamas, Baduhari (India)"),
    (2392	->	"Raga Kokil Pancham (India)"),
    (3145	->	"Raga Kshanika (India)"),
    (3593	->	"Raga Kumarapriya (India)"),
    (2721	->	"Raga Kumurdaki (India)"),
    (2134	->	"Raga Kuntvarali (India)"),
    (2713	->	"Raga Latika (India)"),
    (3144	->	"Raga Lavangi (India)"),
    (2358	->	"Raga Madhukauns (India)"),
    (2263	->	"Raga Madhuri (India)"),
    (2194	->	"Raga Mahathi (India)"),
    (3288	->	"Raga Malahari (India)"),
    (2611	->	"Raga Malarani (India)"),
    (2225	->	"Raga Malashri (India)"),
    (3222	->	"Raga Malayamarutam (India)"),
    (2378	->	"Raga Malkauns (India)"),
    (2197	->	"Raga Mamata (India)"),
    (3218	->	"Raga Manaranjani (India)"),
    (2838	->	"Raga Manavi (India)"),
    (3249	->	"Raga Mandari, Gamakakriya (India)"),
    (2260	->	"Raga Mand (India)"),
    (2390	->	"Raga Manohari (India)"),
    (2582	->	"Raga Matha Kokila (India)"),
    (3272	->	"Raga Megharanjani (India)"),
    (3265	->	"Raga Megharanji (India)"),
    (2643	->	"Raga Megh (India)"),
    (2903	->	"Raga Mian Ki Malhar, Bahar (India)"),
    (2452	->	"Raga Mohanangi (India)"),
    (2725	->	"Raga Mruganandana (India)"),
    (2353	->	"Raga Multani (India)"),
    (3632	->	"Raga Nabhomani (India)"),
    (2645	->	"Raga Nagagandhari (India)"),
    (2769	->	"Raga Nalinakanti, Kedaram (India)"),
    (2385	->	"Raga Nata, Madhuranjani (India)"),
    (2650	->	"Raga Navamanohari (India)"),
    (2860	->	"Raga Neelangi (India)"),
    (2693	->	"Raga Neroshta (India)"),
    (2209	->	"Raga Nigamagamini (India)"),
    (2613	->	"Raga Nishadi (India)"),
    (2096	->	"Raga Ongkari (India)"),
    (3161	->	"Raga Padi (India)"),
    (2783	->	"Raga Pahadi (India)"),
    (2265	->	"Raga Paraju, Simhavahini (India)"),
    (3162	->	"Raga Phenadyuti (India)"),
    (2633	->	"Raga Priyadharshini (India)"),
    (2133	->	"Raga Puruhutika, Purvaholika (India)"),
    (3596	->	"Raga Putrika (India)"),
    (2758	->	"Raga Rageshri, Nattaikurinji (India)"),
    (2759	->	"Raga Ragesri (India)"),
    (3031	->	"Raga Ramdasi Malhar (India)"),
    (3321	->	"Raga Ramkali (India)"),
    (2853	->	"Raga Ranjani, Rangini (India)"),
    (2481	->	"Raga Rasamanjari (India)"),
    (3158	->	"Raga Rasavali (India)"),
    (2629	->	"Raga Rasranjani (India)"),
    (3224	->	"Raga Reva, Revagupti (India)"),
    (3270	->	"Raga Rudra Pancama (India)"),
    (3346	->	"Raga Rukmangi (India)"),
    (3350	->	"Raga Salagavarali (India)"),
    (2354	->	"Raga Samudhra Priya (India)"),
    (2761	->	"Raga Sarasanana (India)"),
    (2614	->	"Raga Sarasvati (India)"),
    (2268	->	"Raga Saravati (India)"),
    (2128	->	"Raga Sarvasri (India)"),
    (3128	->	"Raga Saugandhini, Yashranjani (India)"),
    (3293	->	"Raga Saurashtra (India)"),
    (2330	->	"Raga Shailaja (India)"),
    (2612	->	"Raga Shri Kalyan (India)"),
    (2597	->	"Raga Shubravarni (India)"),
    (2866	->	"Raga Simharava (India)"),
    (4059	->	"Raga Sindhi Bhairavi (India)"),
    (2897	->	"Raga Sindhura Kafi (India)"),
    (2770	->	"Raga Siva Kambhoji, Vivardhini (India)"),
    (3273	->	"Raga Sohini (India)"),
    (2647	->	"Raga Sorati (India)"),
    (2900	->	"Raga Suddha Bangala (India)"),
    (3660	->	"Raga Suddha Mukhari (India)"),
    (3416	->	"Raga Suddha Simantini (India)"),
    (2593	->	"Raga Sumukam (India)"),
    (2872	->	"Raga Syamalam (India)"),
    (2393	->	"Raga Takka (India)"),
    (2259	->	"Raga Tilang, Savitri (India)"),
    (2842	->	"Raga Trimurti (India)"),
    (2609	->	"Raga Vaijayanti (India)"),
    (2198	->	"Raga Valaji (India)"),
    (3274	->	"Raga Vasantabhairavi (India)"),
    (3269	->	"Raga Vasanta, Chayavati (India)"),
    (2868	->	"Raga Vijayanagari (India)"),
    (3633	->	"Raga Vijayasri (India)"),
    (2227	->	"Raga Vijayavasanta (India)"),
    (3401	->	"Raga Viyogavarali (India)"),
    (2230	->	"Raga Vutari (India)"),
    (2740	->	"Raga Yamuna Kalyani (India)"),
    (2264	->	"Raga Zilaf (India)"),
    (3402	->	"Ritsu (Japan)"),
    (2518	->	"Rock 'n' Roll"),
    (2249	->	"Romanian Bacovia"),
    (3254	->	"Romanian Major"),
    (2970	->	"Sabach (Greece)"),
    (3160	->	"Sakura Pentatonic (Japan)"),
    (2114	->	"Sansagari (Japan)"),
    (2644	->	"Scottish Pentatonic"),
    (3220	->	"Scriabin"),
    (3072	->	"Semitone Interval"),
    (3509	->	"Shostakovich Scale"),
    (3562	->	"Spanish Octatonic"),
    (3520	->	"Spanish Pentacluster"),
    (3546	->	"Spanish Phrygian"),
    (3498	->	"Super Locrian"),
    (2642	->	"Suspended Pentatonic"),
    (3835	->	"Symmetrical Decatonic"),
    (2807	->	"Taishikicho, Ryo (Japan)"),
    (2857	->	"Takemitsu Tree Line Mode 1"),
    (2858	->	"Takemitsu Tree Line Mode 2"),
    (2080	->	"Tritone Interval"),
    (4095	->	"Twelve-Tone Chromatic"),
    (3500	->	"Ultra Locrian"),
    (2048	->	"Unison"),
    (2306	->	"Ute Tritonic (North America)"),
    (2907	->	"Utility Minor"),
    (3307	->	"Verdi Enigmatic"),
    (3243	->	"Verdi Enigmatic Ascending"),
    (3275	->	"Verdi Enigmatic Descending"),
    (2818	->	"Warao Tetratonic (South America)"),
    (2560	->	"Wholetone Interval"),
    (2730	->	"Wholetone Scale"),
    (2731	->	"Wholetone Scale With Leading Tone"),
    (2720	->	"Wholetone Tetramirror"),
    (2688	->	"Wholetone Trichord"),
    (3830	->	"Youlan Scale (China)"),
    (2909	->	"Zirafkend (Arabia)")
  )

  //TODO: add G-System to generate all possible equivalence classes of scales up to X "chromatic" notes

  private val names2Ids = ids2Names.map({x => (x._2, x._1) } ).toMap

  val names = ids2Names.values.toList


  /**
   *  Find scale number given a name eg. "Algerian", "Dorian",...
   * @param name
   * @return
   */
  def id(name: String) = names2Ids.get(name)

  /**
   *  Looks up the name given a scale ID (number)
   * @param name
   * @return
   */

  def name(id: Int) = ids2Names.get(id)

  /**
   * Finds nearest scale to the given id (scale number)
   * @param id
   * @return
   */
  def findNearestId(id: Int) = {
    // crude
    var ret = 0
    var minDistance: Option[Int] = None

    ids2Names.foreach({
      tuple =>
        val currentId = tuple._1
        val distance = scala.math.abs(currentId - id)

        if ( minDistance == None || (distance < minDistance.get) ) {
          minDistance = Some(distance)
          ret = currentId
        }
    })

    ret
  }

  //TODO: Fixme, ScaleLib should use bigints here.. or not... may be too many classes for > 2^32
  /**
   * Generates all the unique equivalence classes in a scale with noBits number of pitches
   * @param noBits
   * @return Map -> scale ID (number) -> Class Name
   */
  def generateEquivalenceClasses(noBits: Int = 12) : Map[Int, String] = {
    var map = Map[Int, String]()

    val equivalenceClasses = BitSetOps.equivalenceClasses(noBits).zipWithIndex.map({
      x =>

        (x._1.toInt, "Class "+x._2)
    })

    equivalenceClasses.foreach( x => map = map + x)

    map
  }
}
