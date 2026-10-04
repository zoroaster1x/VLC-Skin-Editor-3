# VLC skins gallery conformance report

Every theme in the official pack at https://images.videolan.org/vlc/skins.html was imported, inspected, validated and rendered by the CLI.

* Themes: 139
* Imported and rendered: 139
* Not rendered: 0
* Layout items seen: 13434
* Validation issues: 490 (errors: 119)
* Render time: median 1845 ms, max 2460 ms

## Validation messages

These are issues the themes already carry, mostly ids shared by two controls or resources and referenced files the download does not ship. VLC resolves duplicate ids by first match, and none of them stop an import or a render. Counts cover the first ten messages per theme.

| Severity | Kind | Count |
|---|---|---|
| error | duplicate item id | 80 |
| error | duplicate resource id | 25 |
| error | missing resource reference | 13 |
| warning | non positive size | 76 |
| warning | missing referenced file | 65 |
| warning | playtree without slider | 14 |
| warning | Color "none" is not #RRGGBB [Window[plWindow]/Layout[plLayout]/Panel[p | 2 |
| warning | Slider "Slider #1" thickness should be positive [Window[maxim]/Layout[ | 2 |
| warning | Layout "playlist_layout" minheight is larger than maxheight [Layout[pl | 1 |
| warning | Color "none" is not #RRGGBB [Window[plwin]/Layout[pllayout]/Group[Grou | 1 |
| warning | Slider "Slider #14" thickness should be positive [Window[minim]/Layout | 1 |
| warning | Slider "Slider #25" thickness should be positive [Window[maxim]/Layout | 1 |
| warning | Slider "Slider #27" thickness should be positive [Window[maxim]/Layout | 1 |

## Per theme

| Theme | Windows | Layouts | Items | Errors | Issues | Render ms | Status |
|---|---|---|---|---|---|---|---|
| Airflow | 4 | 7 | 405 | 0 | 0 | 2453 | ok |
| Alienware Darkstar | 6 | 6 | 83 | 1 | 1 | 2436 | ok |
| argenta | 1 | 3 | 146 | 2 | 4 | 1634 | ok |
| ASkin | 4 | 4 | 124 | 3 | 3 | 2460 | ok |
| ASNO3 | 3 | 3 | 85 | 0 | 0 | 2312 | ok |
| avs_0_95_beta | 5 | 6 | 276 | 0 | 2 | 1890 | ok |
| Bblue | 3 | 3 | 54 | 0 | 0 | 2325 | ok |
| black v2.0 | 4 | 7 | 145 | 0 | 0 | 1504 | ok |
| blackpearl | 1 | 1 | 21 | 0 | 72 | 1604 | ok |
| BlackRed Official | 3 | 3 | 35 | 2 | 3 | 2086 | ok |
| Blend_0-1-4 | 5 | 5 | 108 | 9 | 35 | 2344 | ok |
| Blended-e | 3 | 3 | 50 | 2 | 2 | 2204 | ok |
| Blissta | 4 | 4 | 143 | 0 | 0 | 1804 | ok |
| BlueSteel | 5 | 5 | 110 | 9 | 16 | 1621 | ok |
| bone-deep | 2 | 2 | 69 | 2 | 3 | 1714 | ok |
| Carbon | 4 | 4 | 117 | 11 | 11 | 1833 | ok |
| chaos | 3 | 6 | 168 | 0 | 1 | 1710 | ok |
| chelseaVLC | 1 | 1 | 16 | 0 | 0 | 1925 | ok |
| classic_look | 4 | 4 | 101 | 6 | 13 | 1796 | ok |
| colibri2 | 4 | 4 | 126 | 0 | 0 | 1850 | ok |
| Colibri_1.0 | 5 | 5 | 97 | 6 | 6 | 1632 | ok |
| CompactBlack | 3 | 3 | 63 | 0 | 0 | 1581 | ok |
| Console 1.0 | 2 | 2 | 68 | 0 | 0 | 1762 | ok |
| crossover_leopard | 2 | 5 | 132 | 0 | 0 | 1667 | ok |
| crux | 3 | 3 | 88 | 0 | 9 | 1857 | ok |
| D-GFX_Dark_Skin | 3 | 3 | 33 | 0 | 0 | 1551 | ok |
| Dalin_Media_Player | 2 | 2 | 36 | 0 | 0 | 1998 | ok |
| Dark | 2 | 2 | 55 | 0 | 1 | 1647 | ok |
| dark melody | 3 | 3 | 95 | 0 | 0 | 1827 | ok |
| Dark Pepper | 4 | 4 | 124 | 3 | 3 | 1989 | ok |
| darklounge_vlc_1.0 | 3 | 3 | 109 | 5 | 12 | 2019 | ok |
| darkvoodoo | 1 | 1 | 28 | 0 | 0 | 1990 | ok |
| darkvoodoo2 | 5 | 5 | 115 | 1 | 11 | 2096 | ok |
| DD63 | 2 | 2 | 16 | 0 | 0 | 1723 | ok |
| default_0.8.5 | 5 | 5 | 129 | 0 | 0 | 1950 | ok |
| default_dark wood | 5 | 5 | 129 | 0 | 0 | 1977 | ok |
| default_ebony | 5 | 5 | 129 | 0 | 0 | 1910 | ok |
| default_mod_mentalrey | 4 | 4 | 101 | 6 | 13 | 1916 | ok |
| DefaultRemix | 5 | 5 | 137 | 0 | 0 | 2199 | ok |
| DestroyVLC | 2 | 2 | 69 | 0 | 0 | 2054 | ok |
| dno_black | 3 | 6 | 141 | 0 | 0 | 1742 | ok |
| DPlayer | 5 | 5 | 62 | 1 | 2 | 1802 | ok |
| dzfn-neon | 4 | 4 | 53 | 1 | 39 | 1795 | ok |
| earth ox | 5 | 5 | 95 | 0 | 0 | 1899 | ok |
| earth_red | 5 | 5 | 95 | 0 | 0 | 1923 | ok |
| Ecco_1.0_(ColdBlue_and_FreshGreen)_0.8.6 [Ecco_1.0_(ColdBlue)_0.8.6] | 4 | 4 | 80 | 0 | 0 | 1758 | ok |
| Ecco_1.0_(ColdBlue_and_FreshGreen)_0.8.6 [Ecco_1.0_(FreshGreen)_0.8.6] | 4 | 4 | 80 | 0 | 0 | 1839 | ok |
| Ecco_1.0_(SilverEdition)_0.8.6 | 4 | 4 | 80 | 0 | 0 | 1851 | ok |
| Ecco_1.0_(UbuntuEdition)_0.8.6 | 4 | 4 | 80 | 0 | 0 | 1751 | ok |
| eDark Vlc | 5 | 5 | 102 | 0 | 1 | 1933 | ok |
| electrix | 4 | 4 | 76 | 0 | 2 | 1714 | ok |
| Eminence | 2 | 2 | 52 | 0 | 0 | 1723 | ok |
| FlatScreen | 4 | 4 | 125 | 0 | 0 | 2017 | ok |
| ftouch100 | 1 | 1 | 43 | 1 | 1 | 1976 | ok |
| GGGrey | 3 | 3 | 61 | 1 | 1 | 1924 | ok |
| giga08 | 4 | 4 | 101 | 6 | 13 | 1828 | ok |
| Glow | 3 | 3 | 42 | 0 | 0 | 2049 | ok |
| GNS | 5 | 5 | 101 | 0 | 0 | 1934 | ok |
| Heaven | 4 | 4 | 143 | 0 | 4 | 1953 | ok |
| homecinema | 2 | 2 | 44 | 0 | 0 | 1839 | ok |
| hx_1.1_0.9.0 | 4 | 4 | 109 | 0 | 0 | 1818 | ok |
| hx_milky_1.1_0.9.0 | 4 | 4 | 109 | 0 | 0 | 1758 | ok |
| indigo-deep | 1 | 3 | 110 | 0 | 2 | 1700 | ok |
| Inspired | 1 | 1 | 16 | 0 | 0 | 1954 | ok |
| iphone3g | 1 | 22 | 483 | 0 | 0 | 2150 | ok |
| iPod | 1 | 1 | 22 | 0 | 0 | 1557 | ok |
| itunes | 3 | 4 | 72 | 0 | 0 | 1484 | ok |
| itunes graphite | 2 | 7 | 202 | 0 | 4 | 1572 | ok |
| JVC-VLC3 | 5 | 5 | 102 | 1 | 1 | 1998 | ok |
| kastenhaut | 4 | 4 | 45 | 0 | 0 | 1618 | ok |
| Keagens Deluxe Official | 2 | 2 | 32 | 2 | 11 | 1868 | ok |
| LCARSx32 | 1 | 1 | 42 | 0 | 0 | 2037 | ok |
| marmor | 4 | 4 | 124 | 0 | 0 | 1936 | ok |
| MediaPlayer | 1 | 1 | 53 | 0 | 1 | 2168 | ok |
| miniMatrix | 1 | 1 | 19 | 0 | 0 | 1622 | ok |
| mirror | 4 | 4 | 48 | 0 | 1 | 1746 | ok |
| MM515 | 3 | 3 | 40 | 0 | 0 | 1912 | ok |
| MM616 | 3 | 3 | 39 | 0 | 0 | 2113 | ok |
| Modern | 3 | 5 | 179 | 1 | 1 | 1839 | ok |
| MonsteR_DSi | 2 | 2 | 20 | 0 | 0 | 1975 | ok |
| mpui | 2 | 3 | 51 | 0 | 28 | 1605 | ok |
| my_PSP-black | 3 | 3 | 93 | 0 | 0 | 1856 | ok |
| my_PSP-purple | 4 | 4 | 111 | 0 | 0 | 1959 | ok |
| MySimpleSkin | 5 | 5 | 213 | 0 | 0 | 2167 | ok |
| MyVLCtheme | 1 | 1 | 11 | 0 | 0 | 1558 | ok |
| neon | 4 | 4 | 49 | 1 | 40 | 1690 | ok |
| neXum | 4 | 4 | 158 | 4 | 4 | 1945 | ok |
| Night | 3 | 3 | 41 | 0 | 0 | 1992 | ok |
| Nintendo Black Style | 4 | 4 | 69 | 0 | 1 | 1845 | ok |
| Nintendo Style | 4 | 4 | 69 | 0 | 1 | 2162 | ok |
| OL | 2 | 3 | 51 | 0 | 28 | 1961 | ok |
| ol bleu | 3 | 6 | 168 | 0 | 2 | 1890 | ok |
| Orangeade_1.1 | 5 | 5 | 127 | 0 | 0 | 2007 | ok |
| pardus | 4 | 4 | 111 | 0 | 2 | 2218 | ok |
| pardus_white | 4 | 4 | 87 | 0 | 2 | 2317 | ok |
| Presume | 2 | 2 | 53 | 0 | 0 | 1634 | ok |
| Ps VS. Technobase | 5 | 5 | 103 | 2 | 2 | 1818 | ok |
| psvlc | 2 | 2 | 27 | 0 | 0 | 1857 | ok |
| QuickTime | 2 | 2 | 31 | 0 | 0 | 1590 | ok |
| QuickTime UMX | 3 | 3 | 37 | 0 | 1 | 1620 | ok |
| raptor | 3 | 3 | 119 | 0 | 3 | 1816 | ok |
| redcoast | 4 | 4 | 124 | 2 | 6 | 1998 | ok |
| relaxed | 4 | 4 | 124 | 3 | 3 | 1884 | ok |
| Sam s Glass | 1 | 1 | 13 | 0 | 1 | 1444 | ok |
| sandkastenhaut | 4 | 4 | 45 | 0 | 0 | 1745 | ok |
| serpentine | 1 | 3 | 161 | 1 | 6 | 1943 | ok |
| shiftieVLC | 2 | 2 | 21 | 3 | 3 | 1676 | ok |
| SIMPLy_1.0 | 4 | 4 | 80 | 0 | 0 | 1801 | ok |
| SimplyWhite | 1 | 2 | 37 | 0 | 1 | 1631 | ok |
| skin-deep | 2 | 2 | 66 | 2 | 3 | 1915 | ok |
| sleektouch | 1 | 1 | 14 | 0 | 0 | 1792 | ok |
| Slick Iphone Skin | 3 | 3 | 71 | 0 | 1 | 2062 | ok |
| solar | 1 | 1 | 29 | 0 | 0 | 1763 | ok |
| sony_psp_go | 1 | 25 | 519 | 0 | 0 | 1914 | ok |
| sony_psp_go_black_blue | 1 | 25 | 519 | 0 | 0 | 1793 | ok |
| sony_psp_XL | 1 | 4 | 65 | 0 | 0 | 1942 | ok |
| STRYPER-VLC | 2 | 2 | 29 | 0 | 0 | 1594 | ok |
| subX | 4 | 4 | 101 | 6 | 13 | 1909 | ok |
| THE NEW LOOK | 2 | 2 | 14 | 0 | 0 | 1656 | ok |
| uDeluxe | 3 | 5 | 212 | 0 | 0 | 2080 | ok |
| universe | 4 | 4 | 124 | 0 | 2 | 1864 | ok |
| VLC Darkness | 1 | 3 | 71 | 0 | 1 | 1666 | ok |
| vlc_lcars | 5 | 7 | 214 | 0 | 0 | 1790 | ok |
| vlcforkids | 4 | 5 | 67 | 2 | 2 | 1773 | ok |
| vlcosx | 2 | 2 | 32 | 0 | 0 | 1988 | ok |
| vlctch | 1 | 1 | 10 | 0 | 0 | 1659 | ok |
| void | 3 | 3 | 50 | 0 | 1 | 1675 | ok |
| void-r1 | 3 | 3 | 51 | 0 | 1 | 1670 | ok |
| vplayer | 1 | 1 | 18 | 0 | 0 | 1425 | ok |
| WB | 3 | 5 | 112 | 0 | 0 | 1617 | ok |
| Whiteout | 1 | 1 | 19 | 0 | 0 | 1593 | ok |
| Win_VLC_11 | 2 | 5 | 340 | 0 | 1 | 2079 | ok |
| winamp5 | 2 | 2 | 28 | 0 | 0 | 1524 | ok |
| windows_media_player_12 | 4 | 4 | 124 | 2 | 6 | 1253 | ok |
| WMP11 | 1 | 3 | 148 | 0 | 0 | 2017 | ok |
| wmp11deskband | 1 | 2 | 17 | 0 | 0 | 871 | ok |
| wmp12 | 4 | 4 | 121 | 2 | 6 | 1023 | ok |
| WMP_B_2 | 4 | 4 | 101 | 6 | 13 | 1837 | ok |
| X-Vlc Origins | 4 | 4 | 107 | 1 | 1 | 1982 | ok |

VeLoCity is MIT licensed and is checked separately by tools/recreate-velocity-via-mcp.py; this report covers the VideoLAN gallery archives.
