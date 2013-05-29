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
package org.animotron.bridge.http;

import org.animotron.ATest;
import org.junit.Test;

import static org.animotron.expression.AnimoExpression.__;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 *
 */
public class BootstrapTest extends ATest {

    @Test
    public void test_00() throws Throwable {

        __(
        "def default-theme-settings\n" +

	        "(default, theme-settings)\n" +
	
	        "(link-color            \"#08c\") " +
	
	        "(black                 \"#000\") " +
	        "(gray-dark             \"#333\") " +
	        "(gray                  \"#555\") " +
	        "(gray-light            \"#999\") " +
	        "(gray-lighter          \"#eee\") " +
	        "(white                 \"#fff\") " +
	
	        "(blue                  \"#049CDB\") " +
	        "(blue-dark             \"#0064CD\") " +
	        "(green                 \"#46a546\") " +
	        "(red                   \"#9d261d\") " +
	        "(yellow                \"#ffc40d\") " +
	        "(orange                \"#f89406\") " +
	        "(pink                  \"#c3325f\") " +
	        "(purple                \"#7a43b6\") " +
	
	        "(grid-columns          12) " +
	        "(grid-column-width     \"60px\") " +
	        "(grid-gutter-width     \"20px\") " +
	
	        "(fluid-sidebar-width   \"220px\") " +
	
	        "(base-font-size        \"13px\") " +
	        "(base-font-family      \"'Helvetica Neue', Helvetica, Arial, sans-serif\")\n" +
	        "(base-line-height      \"18px\") " +
	
	        "(primary-color         \"@blue\")"
        ,

        "def bootstrap-variables\n" +

        	"(mime-type text-x-less)\n" +

        	"(any theme-settings)\n" +

        	"(ordered (\"@linkColor: \")            (get link-color) (\"; \") )" +

        	"(ordered (\"@linkColorHover: darken(@linkColor, 15); \") )" +

        	"(ordered (\"@black: \")                (get black) (\"; \") )" +
        	"(ordered (\"@grayDark: \")             (get gray-dark) (\"; \") )" +
        	"(ordered (\"@gray: \")                 (get gray) (\"; \") )" +
        	"(ordered (\"@grayLight: \")            (get gray-light) (\"; \") )" +
        	"(ordered (\"@grayLighter: \")          (get gray-lighter) (\"; \") )" +
        	"(ordered (\"@white: \")                (get white) (\"; \") )" +

        	"(ordered (\"@blue: \")                 (get blue) (\"; \") )" +
        	"(ordered (\"@blueDark: \")             (get blue-dark) (\"; \") )" +
        	"(ordered (\"@green: \")                (get green) (\"; \") )" +
        	"(ordered (\"@red: \")                  (get red) (\"; \") )" +
        	"(ordered (\"@yellow: \")               (get yellow) (\"; \") )" +
        	"(ordered (\"@orange: \")               (get orange) (\"; \") )" +
        	"(ordered (\"@pink: \")                 (get pink) (\"; \") )" +
        	"(ordered (\"@purple: \")               (get purple) (\"; \") )" +

        	"(ordered (\"@gridColumns: \")          (get grid-columns) (\"; \") )" +
        	"(ordered (\"@gridColumnWidth: \")      (get grid-column-width) (\"; \") )" +
        	"(ordered (\"@gridGutterWidth: \")      (get grid-gutter-width) (\"; \") )" +

        	"(ordered (\"@siteWidth: (@gridColumns * @gridColumnWidth) + (@gridGutterWidth * (@gridColumns - 1)); \") )" +

        	"(ordered (\"@fluidSidebarWidth: \")    (get fluid-sidebar-width) (\"; \") )" +

        	"(ordered (\"@zindexDropdown: 1000; @zindexPopover: 1010; @zindexTooltip: 1020; @zindexFixedNavbar: 1030; @zindexModalBackdrop: 1040; @zindexModal: 1050; \") )" +

        	"(ordered (\"@baseFontSize: \")         (get base-font-size) (\"; \") )" +
        	"(ordered (\"@baseFontFamily: \")       (get base-font-family) (\"; \") )" +
        	"(ordered (\"@baseLineHeight: \")       (get base-line-height) (\"; \") )" +

        	"(ordered (\"@primaryButtonColor: \")   (get primary-color) (\";\") )" +
        ""
        );

        assertStringResult(
    		"bootstrap-variables",
    		"@linkColor: #08c; @linkColorHover: darken(@linkColor, 15); @black: #000; @grayDark: #333; @gray: #555; @grayLight: #999; @grayLighter: #eee; @white: #fff; @blue: #049CDB; @blueDark: #0064CD; @green: #46a546; @red: #9d261d; @yellow: #ffc40d; @orange: #f89406; @pink: #c3325f; @purple: #7a43b6; @gridColumns: 12; @gridColumnWidth: 60px; @gridGutterWidth: 20px; @siteWidth: (@gridColumns * @gridColumnWidth) + (@gridGutterWidth * (@gridColumns - 1)); @fluidSidebarWidth: 220px; @zindexDropdown: 1000; @zindexPopover: 1010; @zindexTooltip: 1020; @zindexFixedNavbar: 1030; @zindexModalBackdrop: 1040; @zindexModal: 1050; @baseFontSize: 13px; @baseFontFamily: 'Helvetica Neue', Helvetica, Arial, sans-serif; @baseLineHeight: 18px; @primaryButtonColor: @blue;");
    }

    @Test
    public void test_01() throws Throwable {

        __(
        "def default-theme-settings " +
	
	        "(default, theme-settings) " +
	
	        "(link-color            \"#08c\") " +
	
	        "(black                 \"#000\") " +
	        "(gray-dark             \"#333\") " +
	        "(gray                  \"#555\") " +
	        "(gray-light            \"#999\") " +
	        "(gray-lighter          \"#eee\") " +
	        "(white                 \"#fff\") " +
	
	        "(blue                  \"#049CDB\") " +
	        "(blue-dark             \"#0064CD\") " +
	        "(green                 \"#46a546\") " +
	        "(red                   \"#9d261d\") " +
	        "(yellow                \"#ffc40d\") " +
	        "(orange                \"#f89406\") " +
	        "(pink                  \"#c3325f\") " +
	        "(purple                \"#7a43b6\") " +
	
	        "(grid-columns          12) " +
	        "(grid-column-width     \"60px\") " +
	        "(grid-gutter-width     \"20px\") " +
	
	        "(fluid-sidebar-width   \"220px\") " +
	
	        "(base-font-size        \"13px\") " +
	        "(base-font-family      \"'Helvetica Neue', Helvetica, Arial, sans-serif\") " +
	        "(base-line-height      \"18px\") " +
	
	        "(primary-color         \"@blue\")"
        ,

        "def bootstrap-variables " +

        "(mime-type text-x-less) " +

        "(any theme-settings) " +

        "(ordered (\"@linkColor: \")            (get link-color) (\"; \") )" +

        "(ordered (\"@linkColorHover: darken(@linkColor, 15); \") )" +

        "(ordered (\"@black: \")                (get black) (\"; \") )" +
        "(ordered (\"@grayDark: \")             (get gray-dark) (\"; \") )" +
        "(ordered (\"@gray: \")                 (get gray) (\"; \") )" +
        "(ordered (\"@grayLight: \")            (get gray-light) (\"; \") )" +
        "(ordered (\"@grayLighter: \")          (get gray-lighter) (\"; \") )" +
        "(ordered (\"@white: \")                (get white) (\"; \") )" +

		"(ordered (\"@blue: \")                 (get blue) (\"; \") )" +
		"(ordered (\"@blueDark: \")             (get blue-dark) (\"; \") )" +
		"(ordered (\"@green: \")                (get green) (\"; \") )" +
		"(ordered (\"@red: \")                  (get red) (\"; \") )" +
		"(ordered (\"@yellow: \")               (get yellow) (\"; \") )" +
		"(ordered (\"@orange: \")               (get orange) (\"; \") )" +
		"(ordered (\"@pink: \")                 (get pink) (\"; \") )" +
		"(ordered (\"@purple: \")               (get purple) (\"; \") )" +
		
		"(ordered (\"@gridColumns: \")          (get grid-columns) (\"; \") )" +
		"(ordered (\"@gridColumnWidth: \")      (get grid-column-width) (\"; \") )" +
		"(ordered (\"@gridGutterWidth: \")      (get grid-gutter-width) (\"; \") )" +
		
		"(ordered (\"@siteWidth: (@gridColumns * @gridColumnWidth) + (@gridGutterWidth * (@gridColumns - 1)); \") )" +
		
		"(ordered (\"@fluidSidebarWidth: \")    (get fluid-sidebar-width) (\"; \") )" +
		
		"(ordered (\"@zindexDropdown: 1000; @zindexPopover: 1010; @zindexTooltip: 1020; @zindexFixedNavbar: 1030; @zindexModalBackdrop: 1040; @zindexModal: 1050; \") )" +
		
		"(ordered (\"@baseFontSize: \")         (get base-font-size) (\"; \") )" +
		"(ordered (\"@baseFontFamily: \")       (get base-font-family) (\"; \") )" +
		"(ordered (\"@baseLineHeight: \")       (get base-line-height) (\"; \") )" +
		
		"(ordered (\"@primaryButtonColor: \")   (get primary-color) )"
        );

        assertStringResult("bootstrap-variables", 
    		"@linkColor: #08c; @linkColorHover: darken(@linkColor, 15); @black: #000; @grayDark: #333; @gray: #555; @grayLight: #999; @grayLighter: #eee; @white: #fff; @blue: #049CDB; @blueDark: #0064CD; @green: #46a546; @red: #9d261d; @yellow: #ffc40d; @orange: #f89406; @pink: #c3325f; @purple: #7a43b6; @gridColumns: 12; @gridColumnWidth: 60px; @gridGutterWidth: 20px; @siteWidth: (@gridColumns * @gridColumnWidth) + (@gridGutterWidth * (@gridColumns - 1)); @fluidSidebarWidth: 220px; @zindexDropdown: 1000; @zindexPopover: 1010; @zindexTooltip: 1020; @zindexFixedNavbar: 1030; @zindexModalBackdrop: 1040; @zindexModal: 1050; @baseFontSize: 13px; @baseFontFamily: 'Helvetica Neue', Helvetica, Arial, sans-serif; @baseLineHeight: 18px; @primaryButtonColor: @blue");
    }

    @Test
    public void test_02() throws Throwable {

        __(
        "def default-theme-settings " +
        	"(default, theme-settings) " +
        	"(primary-color         \"@blue\")"
        ,

        "def bootstrap-variables\n" +
        	"(mime-type text-x-less) " +
        	"(any theme-settings) " +
        
        	"(ordered (\"@primaryButtonColor: \")   (get primary-color) (\";\") )"
        );

        assertStringResult("bootstrap-variables", "@primaryButtonColor: @blue;");
    }

    @Test
    public void test_03() throws Throwable {

        __(
        "def default-theme-settings " +
    		"(default, theme-settings) " +
    		"(primary-color         \"@blue\")"
        ,

		"def bootstrap-variables\n" +
			"(mime-type text-x-less)\n" +
			"(any theme-settings)\n" +
			"(ordered (\"@primaryButtonColor: \")   (get primary-color) )"
        );

        assertStringResult("bootstrap-variables", "@primaryButtonColor: @blue");
    }
}