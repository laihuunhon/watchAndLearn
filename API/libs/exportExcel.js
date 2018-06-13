require('node-zip');
var fs = require('fs');

Date.prototype.getJulian = function() {
	return Math.floor((this / 86400000) -
	(this.getTimezoneOffset()/1440) + 2440587.5);
}

Date.prototype.oaDate = function() {
 return (this - new Date(Date.UTC(1899, 12, 29))) / (24 * 60 * 60 * 1000);
}

var templateXLSX = "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";
var sheetFront = '<?xml version="1.0" encoding="utf-8"?><x:worksheet xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:x="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><x:sheetPr><x:outlinePr summaryBelow="1" summaryRight="1" /></x:sheetPr><x:sheetViews><x:sheetView tabSelected="0" workbookViewId="0" /></x:sheetViews><x:sheetFormatPr defaultRowHeight="15" /><x:sheetData>';
var sheetBack = '</x:sheetData><x:printOptions horizontalCentered="0" verticalCentered="0" headings="0" gridLines="0" /><x:pageMargins left="0.75" right="0.75" top="0.75" bottom="0.5" header="0.5" footer="0.75" /><x:pageSetup paperSize="1" scale="100" pageOrder="downThenOver" orientation="default" blackAndWhite="0" draft="0" cellComments="none" errors="displayed" /><x:headerFooter /><x:tableParts count="0" /></x:worksheet>';
var sharedStringsFront = '<?xml version="1.0" encoding="UTF-8"?><x:sst xmlns:x="http://schemas.openxmlformats.org/spreadsheetml/2006/main" uniqueCount="$count" count="$count">';
var sharedStringsBack = '</x:sst>';
var shareStrings;

exports.executeAsync = function(config, type, callBack){
	return process.nextTick(function(){
		var r = exports.execute(config, type);		
		callBack(r);
	});
}

exports.execute = function(data, type, callback){
	// var cols = config.cols,
		// data = config.rows;
	var xlsx = new JSZip(templateXLSX, { base64: true, checkCRC32: false }),
		sheet = xlsx.file("xl/worksheets/sheet.xml"),
		sharedStringsXml = xlsx.file("xl/sharedStrings.xml"),
		styles = xlsx.file("xl/styles.xml"),
		rows = "",
		row ="";
	
	
	// xlsx.remove(sheet.name);
	// xlsx.file(sheet.name, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" mc:Ignorable="x14ac" xmlns:x14ac="http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac"><dimension ref="A1:F34"/><sheetViews><sheetView tabSelected="1" workbookViewId="0"><selection activeCell="F10" sqref="F10"/></sheetView></sheetViews><sheetFormatPr defaultRowHeight="15" x14ac:dyDescent="0.25"/><cols><col min="1" max="1" width="18.7109375" customWidth="1"/><col min="2" max="2" width="17" customWidth="1"/><col min="3" max="3" width="14.85546875" customWidth="1"/><col min="4" max="4" width="15" customWidth="1"/><col min="5" max="5" width="14.85546875" customWidth="1"/><col min="6" max="6" width="15.5703125" customWidth="1"/></cols><sheetData><row r="1" spans="1:4" ht="18.75" x14ac:dyDescent="0.3"><c r="A1" s="2" t="s"><v>0</v></c></row><row r="2" spans="1:4" x14ac:dyDescent="0.25"><c r="A2" s="1" t="s"><v>1</v></c></row><row r="3" spans="1:4" x14ac:dyDescent="0.25"><c r="B3" s="6" t="s"><v>2</v></c><c r="C3" t="s"><v>3</v></c></row><row r="4" spans="1:4" x14ac:dyDescent="0.25"><c r="B4" s="6" t="s"><v>4</v></c><c r="C4" t="s"><v>5</v></c></row><row r="5" spans="1:4" x14ac:dyDescent="0.25"><c r="A5" s="1" t="s"><v>6</v></c></row><row r="6" spans="1:4" x14ac:dyDescent="0.25"><c r="B6" s="6" t="s"><v>7</v></c><c r="C6" t="s"><v>8</v></c></row><row r="7" spans="1:4" x14ac:dyDescent="0.25"><c r="B7" s="6" t="s"><v>9</v></c><c r="C7" t="s"><v>10</v></c></row><row r="8" spans="1:4" x14ac:dyDescent="0.25"><c r="B8" s="6" t="s"><v>11</v></c><c r="C8" t="s"><v>12</v></c></row><row r="9" spans="1:4" x14ac:dyDescent="0.25"><c r="A9" s="1" t="s"><v>13</v></c></row><row r="10" spans="1:4" x14ac:dyDescent="0.25"><c r="B10" s="6" t="s"><v>14</v></c><c r="C10" t="s"><v>15</v></c></row><row r="11" spans="1:4" x14ac:dyDescent="0.25"><c r="B11" s="6" t="s"><v>16</v></c><c r="C11" t="s"><v>17</v></c></row><row r="12" spans="1:4" x14ac:dyDescent="0.25"><c r="B12" s="6" t="s"><v>18</v></c><c r="C12" t="s"><v>17</v></c></row><row r="13" spans="1:4" x14ac:dyDescent="0.25"><c r="B13" s="6" t="s"><v>19</v></c><c r="C13" t="s"><v>12</v></c></row><row r="14" spans="1:4" x14ac:dyDescent="0.25"><c r="B14" s="6" t="s"><v>20</v></c><c r="C14" t="s"><v>17</v></c></row><row r="16" spans="1:4" ht="15.75" x14ac:dyDescent="0.25"><c r="A16" s="5" t="s"><v>21</v></c><c r="B16" s="5"/><c r="C16" s="5"/><c r="D16" s="5"/></row><row r="17" spans="1:6" x14ac:dyDescent="0.25"><c r="A17" s="4" t="s"><v>22</v></c><c r="B17" s="4" t="s"><v>23</v></c><c r="C17" s="4" t="s"><v>24</v></c><c r="D17" s="4" t="s"><v>25</v></c></row><row r="18" spans="1:6" x14ac:dyDescent="0.25"><c r="A18" t="s"><v>26</v></c><c r="B18" s="3" t="s"><v>27</v></c><c r="C18" s="3" t="s"><v>27</v></c><c r="D18" s="3" t="s"><v>27</v></c></row><row r="19" spans="1:6" x14ac:dyDescent="0.25"><c r="A19" t="s"><v>28</v></c><c r="B19" s="3" t="s"><v>27</v></c><c r="C19" s="3" t="s"><v>27</v></c><c r="D19" s="3" t="s"><v>27</v></c></row><row r="20" spans="1:6" x14ac:dyDescent="0.25"><c r="A20" t="s"><v>29</v></c><c r="B20" s="3" t="s"><v>27</v></c><c r="C20" s="3" t="s"><v>27</v></c><c r="D20" s="3" t="s"><v>27</v></c></row><row r="21" spans="1:6" x14ac:dyDescent="0.25"><c r="A21" t="s"><v>30</v></c><c r="B21" s="3" t="s"><v>31</v></c><c r="C21" s="3" t="s"><v>27</v></c><c r="D21" s="3" t="s"><v>31</v></c></row><row r="23" spans="1:6" ht="15.75" x14ac:dyDescent="0.25"><c r="A23" s="5" t="s"><v>32</v></c><c r="B23" s="5"/><c r="C23" s="5"/><c r="D23" s="5"/><c r="E23" s="5"/><c r="F23" s="5"/></row><row r="24" spans="1:6" x14ac:dyDescent="0.25"><c r="A24" s="4" t="s"><v>33</v></c><c r="B24" s="4" t="s"><v>34</v></c><c r="C24" s="4" t="s"><v>35</v></c><c r="D24" s="4" t="s"><v>36</v></c><c r="E24" s="4" t="s"><v>37</v></c><c r="F24" s="4" t="s"><v>38</v></c></row><row r="25" spans="1:6" x14ac:dyDescent="0.25"><c r="A25" t="s"><v>39</v></c><c r="B25" s="3" t="s"><v>27</v></c><c r="C25" s="3" t="s"><v>27</v></c><c r="D25" s="3" t="s"><v>27</v></c><c r="E25" s="3" t="s"><v>27</v></c><c r="F25" s="3" t="s"><v>27</v></c></row><row r="26" spans="1:6" x14ac:dyDescent="0.25"><c r="A26" t="s"><v>40</v></c><c r="B26" s="3" t="s"><v>27</v></c><c r="C26" s="3" t="s"><v>27</v></c><c r="D26" s="3" t="s"><v>27</v></c><c r="E26" s="3" t="s"><v>27</v></c><c r="F26" s="3" t="s"><v>27</v></c></row><row r="27" spans="1:6" x14ac:dyDescent="0.25"><c r="A27" t="s"><v>41</v></c><c r="B27" s="3" t="s"><v>27</v></c><c r="C27" s="3" t="s"><v>27</v></c><c r="D27" s="3" t="s"><v>27</v></c><c r="E27" s="3" t="s"><v>27</v></c><c r="F27" s="3" t="s"><v>27</v></c></row><row r="28" spans="1:6" x14ac:dyDescent="0.25"><c r="A28" t="s"><v>25</v></c><c r="B28" s="3" t="s"><v>27</v></c><c r="C28" s="3" t="s"><v>27</v></c><c r="D28" s="3" t="s"><v>27</v></c><c r="E28" s="3" t="s"><v>27</v></c><c r="F28" s="3" t="s"><v>27</v></c></row><row r="30" spans="1:6" ht="15.75" x14ac:dyDescent="0.25"><c r="A30" s="5" t="s"><v>42</v></c><c r="B30" s="5"/><c r="C30" s="5"/><c r="D30" s="5"/><c r="E30" s="5"/><c r="F30" s="5"/></row><row r="31" spans="1:6" x14ac:dyDescent="0.25"><c r="A31" s="4" t="s"><v>43</v></c><c r="B31" s="4" t="s"><v>34</v></c><c r="C31" s="4" t="s"><v>35</v></c><c r="D31" s="4" t="s"><v>37</v></c><c r="E31" s="4" t="s"><v>38</v></c><c r="F31" s="4" t="s"><v>44</v></c></row><row r="32" spans="1:6" x14ac:dyDescent="0.25"><c r="A32" t="s"><v>45</v></c><c r="B32" s="3" t="s"><v>27</v></c><c r="C32" s="3" t="s"><v>27</v></c><c r="D32" s="3" t="s"><v>27</v></c><c r="E32" s="3" t="s"><v>27</v></c><c r="F32" s="3" t="s"><v>27</v></c></row><row r="33" spans="1:6" x14ac:dyDescent="0.25"><c r="A33" t="s"><v>46</v></c><c r="B33" s="3" t="s"><v>27</v></c><c r="C33" s="3" t="s"><v>27</v></c><c r="D33" s="3" t="s"><v>27</v></c><c r="E33" s="3" t="s"><v>27</v></c><c r="F33" s="3" t="s"><v>27</v></c></row><row r="34" spans="1:6" x14ac:dyDescent="0.25"><c r="A34" t="s"><v>25</v></c><c r="B34" s="3" t="s"><v>27</v></c><c r="C34" s="3" t="s"><v>27</v></c><c r="D34" s="3" t="s"><v>27</v></c><c r="E34" s="3" t="s"><v>27</v></c><c r="F34" s="3" t="s"><v>27</v></c></row></sheetData><mergeCells count="3"><mergeCell ref="A16:D16"/><mergeCell ref="A23:F23"/><mergeCell ref="A30:F30"/></mergeCells><pageMargins left="0.75" right="0.75" top="0.75" bottom="0.5" header="0.5" footer="0.75"/><pageSetup orientation="portrait" r:id="rId1"/></worksheet>');
	// xlsx.remove(sharedStringsXml.name);
	// xlsx.file(sharedStringsXml.name, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="101" uniqueCount="47"><si><t>Analytics</t></si><si><t>Date Range</t></si><si><t>From Date</t></si><si><t>01-01-2013</t></si><si><t>To Date</t></si><si><t>06-01-2013</t></si><si><t>User Range</t></si><si><t>Age From</t></si><si><t>20</t></si><si><t>Age To</t></si><si><t>50</t></si><si><t>Gender</t></si><si><t>undefined</t></si><si><t>Location Range</t></si><si><t>Country</t></si><si><t>us</t></si><si><t>City</t></si><si><t/></si><si><t>State</t></si><si><t>Street</t></si><si><t>Business Name</t></si><si><t>User Analytics</t></si><si><t>Number of</t></si><si><t>iOS</t></si><si><t>Android</t></si><si><t>Total</t></si><si><t>Photos</t></si><si><t>0</t></si><si><t>Reviews</t></si><si><t>Confirms</t></si><si><t>User Registrations</t></si><si><t>1</t></si><si><t>Deal Analytics</t></si><si><t>Deals</t></si><si><t>#</t></si><si><t>$</t></si><si><t>Redeemed</t></si><si><t>Business</t></si><si><t>Active</t></si><si><t>7 days</t></si><si><t>30 days</t></si><si><t>90 days</t></si><si><t>Advertisement Analytics</t></si><si><t>Ads</t></si><si><t>Category</t></si><si><t>Monthly</t></si><si><t>Recurring</t></si></sst>');
	// xlsx.remove(styles.name);
	// xlsx.file(styles.name, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" mc:Ignorable="x14ac" xmlns:x14ac="http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac"><fonts count="4" x14ac:knownFonts="1"><font><sz val="11"/><color rgb="FF000000"/><name val="Calibri"/><family val="2"/></font><font><b/><sz val="11"/><color rgb="FF000000"/><name val="Calibri"/><family val="2"/></font><font><b/><sz val="14"/><color rgb="FF000000"/><name val="Calibri"/><family val="2"/></font><font><b/><sz val="12"/><color rgb="FF000000"/><name val="Calibri"/><family val="2"/></font></fonts><fills count="4"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><gradientFill degree="90"><stop position="0"><color theme="0"/></stop><stop position="1"><color rgb="FFC9C9C9"/></stop></gradientFill></fill><fill><patternFill patternType="solid"><fgColor theme="0" tint="-0.14996795556505021"/><bgColor indexed="64"/></patternFill></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" applyNumberFormat="0" applyBorder="0" applyAlignment="0"/></cellStyleXfs><cellXfs count="7"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyFill="1" applyProtection="1"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1" applyFill="1" applyProtection="1"/><xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1" applyFill="1" applyProtection="1"/><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyFill="1" applyAlignment="1" applyProtection="1"><alignment horizontal="right"/></xf><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1" applyProtection="1"><alignment horizontal="center"/></xf><xf numFmtId="0" fontId="3" fillId="0" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1" applyProtection="1"><alignment horizontal="center"/></xf><xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0" applyFill="1" applyProtection="1"/></cellXfs><cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles><dxfs count="0"/><tableStyles count="0" defaultTableStyle="TableStyleMedium2" defaultPivotStyle="PivotStyleLight16"/><colors><mruColors><color rgb="FFC9C9C9"/><color rgb="FFDDDDDD"/></mruColors></colors><extLst><ext uri="{EB79DEF2-80B8-43e5-95BD-54CBDDF9020C}" xmlns:x14="http://schemas.microsoft.com/office/spreadsheetml/2009/9/main"><x14:slicerStyles defaultSlicerStyle="SlicerStyleLight1"/></ext></extLst></styleSheet>');
// 	
// 	
	// var r = xlsx.generate({ base64: false, compression: "DEFLATE" });
	// delete xlsx;
	// delete shareStrings;
	// callback(r);
	// return;
	
	shareStrings = new Array();
	
	//fill in data
	for (var i = 0; i < data.length; i++) {
		var r = data[i];
		rowValues = data[i].rv;
		row = '<x:row r="' + r.r + '" spans="1:'+ getRowLength(rowValues) + '">';
		for (var j = 0; j < rowValues.length; j++) {
			row = row + addStringCol(getColumnLetter(rowValues[j].c) + r.r, rowValues[j].v);
			
			// switch(cols[j].type) {
				// case 'number':
					// row = row + addNumberCol(getColumnLetter(j+1)+currRow, r[j]);
					// break;
				// case 'date':
					// row = row + addDateCol(getColumnLetter(j+1)+currRow, r[j]);
					// break;
				// case 'bool':
					// row = row + addBoolCol(getColumnLetter(j+1)+currRow, r[j]);
					// break;					
				// default:
					// row = row + addStringCol(getColumnLetter(j+1)+currRow, r[j]);
			// }
		}
		row = row + '</x:row>';
		rows = rows + row;
	}	
	xlsx.remove(sheet.name);
	xlsx.file(sheet.name, sheetFront + rows + sheetBack);
	if (shareStrings.length >0)
	{
		xlsx.remove(sharedStringsXml.name);
		sharedStringsFront = sharedStringsFront.replace(/\$count/g, shareStrings.length);
		xlsx.file(sharedStringsXml.name, sharedStringsFront + convertShareStrings() + sharedStringsBack);
	}
	
	styleXmlFile = './templates/excel/adminAnalytic.xml';
	if (type == 2) styleXmlFile = './templates/excel/businessAnalytic.xml';
	
	fs.readFile(styleXmlFile, {encoding: 'utf-8', flag: 'r'}, function(err, xmlContent) {
		if (err) throw err;
		xlsx.file(styles.name, xmlContent);
		var r = xlsx.generate({ base64: false, compression: "DEFLATE" });
		delete xlsx;
		delete shareStrings;
		callback(r);
	});
}

var addNumberCol = function(cellRef, value){
	if (value===null)
		return "";
	else
		return '<x:c r="'+cellRef+'" s="0" t="n"><x:v>'+value+'</x:v></x:c>';
};

var addDateCol = function(cellRef, value){
	if (value===null)
		return "";
	else
		return '<x:c r="'+cellRef+'" s="1" t="n"><x:v>'+value+'</x:v></x:c>';
};

var addBoolCol = function(cellRef, value){
	if (value===null)
		return "";
	if (value){
	  value = 1
	} else
	  value = 0;
	return '<x:c r="'+cellRef+'" s="0" t="b"><x:v>'+value+'</x:v></x:c>';
};
var addStringCol = function(cellRef, value){
	if (value===null)
		return "";
  if (typeof value ==='string'){
    value = value.replace(/&/g, "&amp;").replace(/'/g, "&apos;").replace(/>/g, "&gt;").replace(/</g, "&lt;");
  }
  
	if (shareStrings.indexOf(value) < 0){
		shareStrings.push(value);
	}
	return '<x:c r="'+cellRef+'" s="0" t="s"><x:v>'+shareStrings.indexOf(value)+'</x:v></x:c>';
};

var convertShareStrings = function(){
	var r = "";
	for (var i=0;i<shareStrings.length;i++)
	{	
		r = r + "<x:si><x:t>"+shareStrings[i]+"</x:t></x:si>";
	}
	return r;
};

var getColumnLetter = function(col){
  if (col <= 0)
	throw "col must be more than 0";
  var array = new Array();
  while (col > 0)
  {
	var remainder = col % 26;
	col /= 26;
	col = Math.floor(col);
	if(remainder ===0)
	{
		remainder = 26;
		col--;
	}
	array.push(64 + remainder);
  }
  return String.fromCharCode.apply(null, array.reverse());
}

var getRowLength = function(rowValues) {
	if (rowValues.length == 0) return 0;
	
	maxLength = 0;
	for (var i = 0; i < rowValues.length; i++) {
		if (rowValues[i].c > maxLength) maxLength = rowValues[i].c;
	}
	
	return maxLength;
}
