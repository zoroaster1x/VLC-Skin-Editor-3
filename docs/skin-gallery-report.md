# VLC skins gallery conformance report

Every theme in the official pack at https://images.videolan.org/vlc/skins.html was imported, inspected, validated and rendered by the CLI.

* Themes: 123
* Imported and rendered: 123
* Not rendered: 0
* Layout items seen: 12336
* Validation issues: 550 (errors: 133)
* Render time: median 959 ms, max 2249 ms

## Validation messages

These are issues the themes already carry, mostly ids shared by two controls or resources and referenced files the download does not ship. VLC resolves duplicate ids by first match, and none of them stop an import or a render. Counts cover the first ten messages per theme.

| Severity | Kind | Count |
|---|---|---|
| error | duplicate item id | 76 |
| error | duplicate resource id | 27 |
| error | missing resource reference | 21 |
| warning | non positive size | 83 |
| warning | missing referenced file | 65 |
| warning | playtree without slider | 14 |
| warning | Color "none" is not #RRGGBB [Window[plWindow]/Layout[plLayout]/Panel[p | 2 |
| warning | Slider "Slider #1" thickness should be positive [Window[maxim]/Layout[ | 2 |
| warning | Color "" is not #RRGGBB [Window[maxa]/Layout[pll]/Playtree[plt]] | 2 |
| warning | Layout "playlist_layout" minheight is larger than maxheight [Layout[pl | 1 |
| warning | Color "none" is not #RRGGBB [Window[plwin]/Layout[pllayout]/Group[Grou | 1 |
| warning | Slider "Slider #14" thickness should be positive [Window[minim]/Layout | 1 |
| warning | Slider "Slider #25" thickness should be positive [Window[maxim]/Layout | 1 |

## Per theme

| Theme | Windows | Layouts | Items | Errors | Issues | Render ms | Status |
|---|---|---|---|---|---|---|---|
| Airflow | 4 | 7 | 405 | 0 | 0 | 1637 | ok |
| argenta | 1 | 3 | 146 | 2 | 4 | 1571 | ok |
| ASkin | 4 | 4 | 124 | 3 | 3 | 1329 | ok |
| ASNO3 | 3 | 3 | 85 | 0 | 0 | 1210 | ok |
| avs_0_95_beta | 5 | 6 | 276 | 0 | 2 | 1950 | ok |
| Bblue | 3 | 3 | 54 | 0 | 0 | 1211 | ok |
| blackpearl | 1 | 1 | 21 | 0 | 72 | 1566 | ok |
| Blend | 4 | 4 | 96 | 9 | 35 | 1483 | ok |
| Blend_0-1-4 | 5 | 5 | 108 | 9 | 35 | 1378 | ok |
| Blended-e | 3 | 3 | 50 | 2 | 2 | 1321 | ok |
| Blissta | 4 | 4 | 143 | 0 | 0 | 1105 | ok |
| BlueSteel | 5 | 5 | 110 | 9 | 16 | 731 | ok |
| bone-deep | 2 | 2 | 69 | 2 | 3 | 1578 | ok |
| Carbon | 4 | 4 | 117 | 11 | 11 | 857 | ok |
| chaos | 3 | 6 | 168 | 6 | 7 | 1694 | ok |
| chelseaVLC | 1 | 1 | 16 | 0 | 0 | 1706 | ok |
| classic_look | 4 | 4 | 101 | 6 | 13 | 1684 | ok |
| colibri2 | 4 | 4 | 126 | 0 | 0 | 1637 | ok |
| Colibri_1.0 | 5 | 5 | 97 | 6 | 6 | 718 | ok |
| CompactBlack | 3 | 3 | 63 | 0 | 0 | 564 | ok |
| crossover_leopard | 2 | 5 | 132 | 0 | 0 | 1330 | ok |
| crux | 3 | 3 | 88 | 0 | 9 | 1685 | ok |
| D-GFX_Dark_Skin | 3 | 3 | 33 | 0 | 0 | 618 | ok |
| Dalin_Media_Player | 2 | 2 | 36 | 0 | 0 | 1653 | ok |
| Dark | 2 | 2 | 55 | 0 | 1 | 724 | ok |
| darklounge_vlc_1.0 | 3 | 3 | 109 | 5 | 12 | 2008 | ok |
| darkvoodoo | 1 | 1 | 28 | 0 | 0 | 2249 | ok |
| darkvoodoo2 | 5 | 5 | 115 | 1 | 11 | 1869 | ok |
| DD63 | 2 | 2 | 16 | 0 | 0 | 795 | ok |
| default_0.8.5 | 5 | 5 | 129 | 0 | 0 | 1849 | ok |
| default_ebony | 5 | 5 | 129 | 0 | 0 | 1770 | ok |
| default_mod_mentalrey | 4 | 4 | 101 | 6 | 13 | 1784 | ok |
| DefaultRemix | 5 | 5 | 137 | 0 | 0 | 801 | ok |
| DestroyVLC | 2 | 2 | 69 | 0 | 0 | 854 | ok |
| dno_black | 3 | 6 | 141 | 0 | 0 | 1431 | ok |
| DPlayer | 5 | 5 | 62 | 1 | 2 | 1289 | ok |
| dzfn-neon | 4 | 4 | 53 | 1 | 39 | 1499 | ok |
| earth_red | 5 | 5 | 95 | 0 | 0 | 708 | ok |
| Ecco_1.0_(ColdBlue_and_FreshGreen)_0.8.6 [Ecco_1.0_(ColdBlue)_0.8.6] | 4 | 4 | 80 | 0 | 0 | 637 | ok |
| Ecco_1.0_(ColdBlue_and_FreshGreen)_0.8.6 [Ecco_1.0_(FreshGreen)_0.8.6] | 4 | 4 | 80 | 0 | 0 | 1487 | ok |
| Ecco_1.0_(SilverEdition)_0.8.6 | 4 | 4 | 80 | 0 | 0 | 965 | ok |
| Ecco_1.0_(UbuntuEdition)_0.8.6 | 4 | 4 | 80 | 0 | 0 | 1451 | ok |
| eivx-wmp12 | 5 | 5 | 138 | 4 | 12 | 881 | ok |
| electrix | 4 | 4 | 76 | 0 | 2 | 689 | ok |
| Eminence | 2 | 2 | 52 | 0 | 0 | 1462 | ok |
| FlatScreen | 4 | 4 | 125 | 0 | 0 | 1265 | ok |
| ftouch100 | 1 | 1 | 43 | 1 | 1 | 800 | ok |
| GGGrey | 3 | 3 | 61 | 1 | 1 | 812 | ok |
| giga08 | 4 | 4 | 101 | 6 | 13 | 692 | ok |
| Glow | 3 | 3 | 42 | 0 | 0 | 1243 | ok |
| GNS | 5 | 5 | 101 | 0 | 0 | 924 | ok |
| Heaven | 4 | 4 | 143 | 0 | 4 | 1232 | ok |
| homecinema | 2 | 2 | 44 | 0 | 0 | 782 | ok |
| hx_1.1_0.9.0 | 4 | 4 | 109 | 0 | 0 | 815 | ok |
| hx_milky_1.1_0.9.0 | 4 | 4 | 109 | 0 | 0 | 742 | ok |
| indigo-deep | 1 | 3 | 110 | 0 | 2 | 874 | ok |
| Inspired | 1 | 1 | 16 | 0 | 0 | 1404 | ok |
| iphone3g | 1 | 22 | 483 | 0 | 0 | 1046 | ok |
| iPod | 1 | 1 | 22 | 0 | 0 | 738 | ok |
| itunes | 3 | 4 | 72 | 0 | 0 | 557 | ok |
| JVC-VLC3 | 5 | 5 | 102 | 1 | 1 | 1636 | ok |
| kastenhaut | 4 | 4 | 45 | 0 | 0 | 607 | ok |
| LCARSx32 | 1 | 1 | 42 | 0 | 0 | 1709 | ok |
| marmor | 4 | 4 | 124 | 0 | 0 | 788 | ok |
| MediaPlayer | 1 | 1 | 53 | 0 | 1 | 1826 | ok |
| miniMatrix | 1 | 1 | 19 | 0 | 0 | 596 | ok |
| mirror | 4 | 4 | 48 | 0 | 1 | 669 | ok |
| MM515 | 3 | 3 | 40 | 0 | 0 | 1572 | ok |
| MM616 | 3 | 3 | 39 | 0 | 0 | 1561 | ok |
| Modern | 3 | 5 | 179 | 1 | 1 | 1555 | ok |
| MonsteR_DSi | 2 | 2 | 20 | 0 | 0 | 1680 | ok |
| mpui | 2 | 3 | 51 | 0 | 28 | 558 | ok |
| my_PSP-black | 3 | 3 | 93 | 0 | 0 | 722 | ok |
| my_PSP-purple | 4 | 4 | 111 | 0 | 0 | 834 | ok |
| MySimpleSkin | 5 | 5 | 213 | 0 | 0 | 1920 | ok |
| MyVLCtheme | 1 | 1 | 11 | 0 | 0 | 1287 | ok |
| neon | 4 | 4 | 49 | 1 | 40 | 627 | ok |
| neXum | 4 | 4 | 158 | 4 | 4 | 808 | ok |
| Night | 3 | 3 | 41 | 0 | 0 | 1568 | ok |
| OL | 2 | 3 | 51 | 0 | 28 | 1379 | ok |
| Orangeade_1.1 | 5 | 5 | 127 | 0 | 0 | 1801 | ok |
| pardus | 4 | 4 | 111 | 0 | 2 | 920 | ok |
| pardus_white | 4 | 4 | 87 | 0 | 2 | 915 | ok |
| Presume | 2 | 2 | 53 | 0 | 0 | 1508 | ok |
| psvlc | 2 | 2 | 27 | 0 | 0 | 739 | ok |
| QuickTime | 2 | 2 | 31 | 0 | 0 | 1519 | ok |
| raptor | 3 | 3 | 119 | 0 | 5 | 669 | ok |
| redcoast | 4 | 4 | 124 | 2 | 6 | 1003 | ok |
| relaxed | 4 | 4 | 124 | 3 | 3 | 802 | ok |
| sandkastenhaut | 4 | 4 | 45 | 0 | 0 | 615 | ok |
| serpentine | 1 | 3 | 161 | 1 | 6 | 715 | ok |
| shiftieVLC | 2 | 2 | 21 | 3 | 3 | 818 | ok |
| SIMPLy_1.0 | 4 | 4 | 80 | 0 | 0 | 1664 | ok |
| SimplyWhite | 1 | 2 | 37 | 0 | 1 | 1503 | ok |
| skin-deep | 2 | 2 | 66 | 2 | 3 | 959 | ok |
| sleektouch | 1 | 1 | 14 | 0 | 0 | 1010 | ok |
| solar | 1 | 1 | 29 | 0 | 0 | 872 | ok |
| sony_psp_go | 1 | 25 | 519 | 0 | 0 | 754 | ok |
| sony_psp_go_black_blue | 1 | 25 | 519 | 0 | 0 | 771 | ok |
| sony_psp_XL | 1 | 4 | 65 | 0 | 0 | 860 | ok |
| STRYPER-VLC | 2 | 2 | 29 | 0 | 0 | 1480 | ok |
| subX | 4 | 4 | 101 | 6 | 13 | 742 | ok |
| truc-2 | 1 | 3 | 148 | 0 | 0 | 857 | ok |
| uDeluxe | 3 | 5 | 212 | 0 | 0 | 787 | ok |
| universe | 4 | 4 | 124 | 0 | 2 | 641 | ok |
| vlc-default | 5 | 7 | 184 | 0 | 13 | 539 | ok |
| vlc-winamp2 | 3 | 6 | 168 | 6 | 26 | 452 | ok |
| vlc_lcars | 5 | 7 | 214 | 0 | 0 | 709 | ok |
| vlcforkids | 4 | 5 | 67 | 2 | 2 | 625 | ok |
| vlcosx | 2 | 2 | 32 | 0 | 0 | 628 | ok |
| vlctch | 1 | 1 | 10 | 0 | 0 | 524 | ok |
| void | 3 | 3 | 50 | 0 | 1 | 572 | ok |
| void-r1 | 3 | 3 | 51 | 0 | 1 | 628 | ok |
| vplayer | 1 | 1 | 18 | 0 | 0 | 571 | ok |
| WB | 3 | 5 | 112 | 0 | 0 | 1497 | ok |
| Whiteout | 1 | 1 | 19 | 0 | 0 | 1496 | ok |
| Win_VLC_11 | 2 | 5 | 340 | 0 | 1 | 1798 | ok |
| winamp5 | 2 | 2 | 28 | 0 | 0 | 582 | ok |
| windows_media_player_12 | 4 | 4 | 124 | 2 | 6 | 813 | ok |
| WMP11 | 1 | 3 | 148 | 0 | 0 | 1873 | ok |
| wmp11deskband | 1 | 2 | 17 | 0 | 0 | 406 | ok |
| wmp12 | 4 | 4 | 121 | 2 | 6 | 642 | ok |
| WMP_B_2 | 4 | 4 | 101 | 6 | 13 | 1576 | ok |

VeLoCity is MIT licensed and is checked separately by tools/recreate-velocity-via-mcp.py; this report covers the VideoLAN gallery plus the two themes VLC itself ships.
