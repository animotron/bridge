/*
 *  Copyright (C) 2011-2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.animotron.bridge.web;

import org.animotron.ATest;
import org.animotron.expression.AnimoExpression;
import org.junit.Test;

import static org.animotron.expression.Expression.__;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class BootstrapTest extends ATest {

    @Test
    public void test_00() throws Exception {

        __(
                new AnimoExpression(
                        "the default-theme-settings\n" +
                                "\n" +
                                "    (default, theme-settings)\n" +
                                "\n" +
                                "    (link-color            \"#08c\")\n" +
                                "\n" +
                                "    (black                 \"#000\")\n" +
                                "    (gray-dark             \"#333\")\n" +
                                "    (gray                  \"#555\")\n" +
                                "    (gray-light            \"#999\")\n" +
                                "    (gray-lighter          \"#eee\")\n" +
                                "    (white                 \"#fff\")\n" +
                                "\n" +
                                "    (blue                  \"#049CDB\")\n" +
                                "    (blue-dark             \"#0064CD\")\n" +
                                "    (green                 \"#46a546\")\n" +
                                "    (red                   \"#9d261d\")\n" +
                                "    (yellow                \"#ffc40d\")\n" +
                                "    (orange                \"#f89406\")\n" +
                                "    (pink                  \"#c3325f\")\n" +
                                "    (purple                \"#7a43b6\")\n" +
                                "\n" +
                                "\n" +
                                "    (grid-columns          12)\n" +
                                "    (grid-column-width     \"60px\")\n" +
                                "    (grid-gutter-width     \"20px\")\n" +
                                "\n" +
                                "    (fluid-sidebar-width   \"220px\")\n" +
                                "\n" +
                                "    (base-font-size        \"13px\")\n" +
                                "    (base-font-family      \"'Helvetica Neue', Helvetica, Arial, sans-serif\")\n" +
                                "    (base-line-height      \"18px\")\n" +
                                "\n" +
                                "    (primary-color         \"@blue\")"
                ),

                new AnimoExpression(
                        "the bootstrap-variables\n" +
                                "\n" +
                                "    (mime-type text-x-less)\n" +
                                "\n" +
                                "    (any theme-settings)\n" +
                                "\n" +
                                "    (\"@linkColor: \")            (get link-color) (\"; \")\n" +
                                "\n" +
                                "    (\"@linkColorHover: darken(@linkColor, 15); \")\n" +
                                "\n" +
                                "    (\"@black: \")                (get black) (\"; \")\n" +
                                "    (\"@grayDark: \")             (get gray-dark) (\"; \")\n" +
                                "    (\"@gray: \")                 (get gray) (\"; \")\n" +
                                "    (\"@grayLight: \")            (get gray-light) (\"; \")\n" +
                                "    (\"@grayLighter: \")          (get gray-lighter) (\"; \")\n" +
                                "    (\"@white: \")                (get white) (\"; \")\n" +
                                "\n" +
                                "    (\"@blue: \")                 (get blue) (\"; \")\n" +
                                "    (\"@blueDark: \")             (get blue-dark) (\"; \")\n" +
                                "    (\"@green: \")                (get green) (\"; \")\n" +
                                "    (\"@red: \")                  (get red) (\"; \")\n" +
                                "    (\"@yellow: \")               (get yellow) (\"; \")\n" +
                                "    (\"@orange: \")               (get orange) (\"; \")\n" +
                                "    (\"@pink: \"                  (get pink) (\"; \")\n" +
                                "    (\"@purple: \")               (get purple) (\"; \")\n" +
                                "\n" +
                                "    (\"@gridColumns: \")          (get grid-columns) (\"; \")\n" +
                                "    (\"@gridColumnWidth: \")      (get grid-column-width) (\"; \")\n" +
                                "    (\"@gridGutterWidth: \")      (get grid-gutter-width) (\"; \")\n" +
                                "\n" +
                                "    (\"@siteWidth: (@gridColumns * @gridColumnWidth) + (@gridGutterWidth * (@gridColumns - 1)); \")\n" +
                                "\n" +
                                "    (\"@fluidSidebarWidth: \")    (get fluid-sidebar-width) (\"; \")\n" +
                                "\n" +
                                "    (\"@zindexDropdown: 1000; @zindexPopover: 1010; @zindexTooltip: 1020; @zindexFixedNavbar: 1030; @zindexModalBackdrop: 1040; @zindexModal: 1050; \")\n" +
                                "\n" +
                                "    (\"@baseFontSize: \")         (get base-font-size) (\"; \")\n" +
                                "    (\"@baseFontFamily: \")       (get base-font-family) (\"; \")\n" +
                                "    (\"@baseLineHeight: \")       (get base-line-height) (\"; \")\n" +
                                "\n" +
                                "    (\"@primaryButtonColor: \")   (get primary-color) (\";\")"
                )

        );


        assertStringResult(new AnimoExpression("bootstrap-variables"), "");


    }

    @Test
    public void test_01() throws Exception {

        __(
                new AnimoExpression(
                        "the default-theme-settings\n" +
                                "\n" +
                                "    (default, theme-settings)\n" +
                                "\n" +
                                "    (link-color            \"#08c\")\n" +
                                "\n" +
                                "    (black                 \"#000\")\n" +
                                "    (gray-dark             \"#333\")\n" +
                                "    (gray                  \"#555\")\n" +
                                "    (gray-light            \"#999\")\n" +
                                "    (gray-lighter          \"#eee\")\n" +
                                "    (white                 \"#fff\")\n" +
                                "\n" +
                                "    (blue                  \"#049CDB\")\n" +
                                "    (blue-dark             \"#0064CD\")\n" +
                                "    (green                 \"#46a546\")\n" +
                                "    (red                   \"#9d261d\")\n" +
                                "    (yellow                \"#ffc40d\")\n" +
                                "    (orange                \"#f89406\")\n" +
                                "    (pink                  \"#c3325f\")\n" +
                                "    (purple                \"#7a43b6\")\n" +
                                "\n" +
                                "\n" +
                                "    (grid-columns          12)\n" +
                                "    (grid-column-width     \"60px\")\n" +
                                "    (grid-gutter-width     \"20px\")\n" +
                                "\n" +
                                "    (fluid-sidebar-width   \"220px\")\n" +
                                "\n" +
                                "    (base-font-size        \"13px\")\n" +
                                "    (base-font-family      \"'Helvetica Neue', Helvetica, Arial, sans-serif\")\n" +
                                "    (base-line-height      \"18px\")\n" +
                                "\n" +
                                "    (primary-color         \"@blue\")"
                ),

                new AnimoExpression(
                        "the bootstrap-variables\n" +
                                "\n" +
                                "    (mime-type text-x-less)\n" +
                                "\n" +
                                "    (any theme-settings)\n" +
                                "\n" +
                                "    (\"@linkColor: \")            (get link-color) (\"; \")\n" +
                                "\n" +
                                "    (\"@linkColorHover: darken(@linkColor, 15); \")\n" +
                                "\n" +
                                "    (\"@black: \")                (get black) (\"; \")\n" +
                                "    (\"@grayDark: \")             (get gray-dark) (\"; \")\n" +
                                "    (\"@gray: \")                 (get gray) (\"; \")\n" +
                                "    (\"@grayLight: \")            (get gray-light) (\"; \")\n" +
                                "    (\"@grayLighter: \")          (get gray-lighter) (\"; \")\n" +
                                "    (\"@white: \")                (get white) (\"; \")\n" +
                                "\n" +
                                "    (\"@blue: \")                 (get blue) (\"; \")\n" +
                                "    (\"@blueDark: \")             (get blue-dark) (\"; \")\n" +
                                "    (\"@green: \")                (get green) (\"; \")\n" +
                                "    (\"@red: \")                  (get red) (\"; \")\n" +
                                "    (\"@yellow: \")               (get yellow) (\"; \")\n" +
                                "    (\"@orange: \")               (get orange) (\"; \")\n" +
                                "    (\"@pink: \"                  (get pink) (\"; \")\n" +
                                "    (\"@purple: \")               (get purple) (\"; \")\n" +
                                "\n" +
                                "    (\"@gridColumns: \")          (get grid-columns) (\"; \")\n" +
                                "    (\"@gridColumnWidth: \")      (get grid-column-width) (\"; \")\n" +
                                "    (\"@gridGutterWidth: \")      (get grid-gutter-width) (\"; \")\n" +
                                "\n" +
                                "    (\"@siteWidth: (@gridColumns * @gridColumnWidth) + (@gridGutterWidth * (@gridColumns - 1)); \")\n" +
                                "\n" +
                                "    (\"@fluidSidebarWidth: \")    (get fluid-sidebar-width) (\"; \")\n" +
                                "\n" +
                                "    (\"@zindexDropdown: 1000; @zindexPopover: 1010; @zindexTooltip: 1020; @zindexFixedNavbar: 1030; @zindexModalBackdrop: 1040; @zindexModal: 1050; \")\n" +
                                "\n" +
                                "    (\"@baseFontSize: \")         (get base-font-size) (\"; \")\n" +
                                "    (\"@baseFontFamily: \")       (get base-font-family) (\"; \")\n" +
                                "    (\"@baseLineHeight: \")       (get base-line-height) (\"; \")\n" +
                                "\n" +
                                "    (\"@primaryButtonColor: \")   (get primary-color)"
                )

        );


        assertStringResult(new AnimoExpression("bootstrap-variables"), "");


    }

    @Test
    public void test_02() throws Exception {

        __(
                new AnimoExpression(
                        "the default-theme-settings\n" +
                                "\n" +
                                "    (default, theme-settings)\n" +
                                "\n" +
                                "    (primary-color         \"@blue\")"
                ),

                new AnimoExpression(
                        "the bootstrap-variables\n" +
                                "\n" +
                                "    (mime-type text-x-less)\n" +
                                "\n" +
                                "    (any theme-settings)\n" +
                                "\n" +
                                "    (\"@primaryButtonColor: \")   (get primary-color) (\";\")"
                )

        );


        assertStringResult(new AnimoExpression("bootstrap-variables"), "@primaryButtonColor: @blue;");


    }

    @Test
    public void test_03() throws Exception {

        __(
                new AnimoExpression(
                        "the default-theme-settings\n" +
                                "\n" +
                                "    (default, theme-settings)\n" +
                                "\n" +
                                "    (primary-color         \"@blue\")"
                ),

                new AnimoExpression(
                        "the bootstrap-variables\n" +
                                "\n" +
                                "    (mime-type text-x-less)\n" +
                                "\n" +
                                "    (any theme-settings)\n" +
                                "\n" +
                                "    (\"@primaryButtonColor: \")   (get primary-color)"
                )

        );


        assertStringResult(new AnimoExpression("bootstrap-variables"), "@primaryButtonColor: @blue");


    }


}