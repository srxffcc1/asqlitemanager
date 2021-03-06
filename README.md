# asqlitemanager
Download binaries here: http://market.android.com/details?pub=Andsen

Version (3.3):
	A new CREATE TABLE wizard where entered fields can be edited / deleted
	Data entry / editor tool allow insert, update and delete on views
		with instead of insert, update and delete triggers 
	Sort data in table viewer by clicking on title of column
	Font size in directory and table lists follow the font size of data grids
	Now check if database exists before trying to open recently opened databases
		and removes deleted databases from the list

	Bug fixes:
		Welcome and Tip screens now works with with with change in orientation
		Table definitions shows triggers regardless of case
		Data entry / editor tool now handle strings with quotation marks
		Paging and filter are no longer reset during rotation of device

Version (3.2):
	Select where to create a new database using a directory picker
	You get tips about what is possible in the different parts of the program
	Foreign key selection lists now show a string representation of the records
		not just the FK fields. The field that the foreign key refers to MUST be 
		the primary key in the referenced table
	More checking in create table wizard
	Corrections of the French translation (was by my mistake included in 
		version 3.1)
	Bug fixes (hopefully solved)
		java.lang.StringIndexOutOfBoundsException:
			at dk.andsen.asqlitemanager.Database.insertRecord(Database.java:1471
		java.lang.NullPointerException
			at dk.andsen.asqlitemanager.DBViewer.onClick (DBViewer.java:377)
		java.lang.NullPointerException
			at dk.andsen.asqlitemanager.DBViewer.onDestroy(DBViewer.java:201)
		show table info with no valid database open, test for valid or empty FK
			definition)
	
Version (3.1):
	Go directly to favorite view in table viewer (Fields, SQL or Data)
	Configurable font size in data lists
	Easy editing of databases in "Dropbox"
	Bug fixes: Has solved an annoying null pointer bug - it did appear if
		users was trying to manipulate something that was not a database  

Version (3.0):
	A tool to create new databases with PK, ASC / DESC, AUTOINCREMENT,
	  not null, unique, default values, foreign keys and check for field type.
	Easy deletion of tables, views and indexes from lists in database viewer by
	  "long clicking" the table / view / index
	Foreign key constraint checking can be turned on (Android default is off)
	Better database handling hopefully giving fewer errors / force closes
	Now works in landscape mode
	Better testing of databases on opening
	Bug fixes

Version 2.7:
	The following Android 3 has been problems solved:
		Problems with creating tables and executing other than select statements
		Now name of databases can be in mixed case
  Editing foreign key fields using selection list based on foreign key values
    (experimental and must be turned on in configuration)
  Now keeping main screen in portrait mode must be configured
		Logging can be turned on to collect informations about bugs (use
	    aLogViewer and filter for aSQLMan)
  Bug fixes

Version 2.6:
  Main screen now always in portrait
  First / last page in table viewer
  Simple filter option tin table viewer
  Quite a few bug fixes

Version 2.5:
  Bug fixes
  Hope the BLOB problem that some users gets is solved
  Now records can be deleted by pressing edit in the table viewer
  Can export and restore tables with BLOB fields
  Most of the functions are listed at the Help screen

Version 2.4:
  Now support table and field names with spaces
  Colours in table viewers data grid showing the type of the fields
  Tables with BLOB fields can be exported (but not imported)
  Quite a few bug fixes
  Catch errors on updates and inserts and tell what went wrong
  
Version 2.3:
  Progress bar during export of databases
  New Database info menu in Database viewer 
  Retrieving of preferences bug fixed
  No longer "force close" in Query builder if fields selected before tables

Version 2.2: 
  Minor change to work with aShell
  
Version 2.1:
  Bugs fixed (9 out of 12 reported. Too little information in the last three to be
    located:
    NumberFormatException When displaying and editing records where rowId
	  can not be stored in a Java int
	SQLiteException when trying to create new database in nonexistent path
	SQLiteException when trying to edit sqlite_master (can't be updated manually)
  Minor improvements:
	SQL in query viewer is now saved on exit and restored on load
		
Version (2.0):
  Tables can be dumped from "Table view" to SQL file (that can later be loaded 
    by execute script)
  Recent files - easy open recently opened databases (cleared on reset) 
  Number of recent files configurable
  Bug when displaying and querying tables with BLOB fields fixed
    BLOB fields set to editable = false
  Reset also restores the welcome screen
  Export (to ASCII file) now don't throw exception if missing or invalid SQL
  	but gives error
  Messages after export and restore and better error handling in places
    
Working on:
	Version with root support.
  Planning for further development, feature request are welcome, could be:
	Sort data / query results by clicking on title
	Implement some of SQLites .commands:
	  .import FILE TABLE	import data from text file to table
	"Tab" view / show active tab
	Scripts editor?
  
Version 1.2.2
  News screen on open
  Bug with displaying data form views fixed
  
Version 1.2.1
  Bug with displaying data form views fixed

Version 1.2
  Correctly scaling on the Galaxy Tab and hopefully on other large displays
  Open SQL files (from database view) and executed all or single line
  Error messages during restore and execution of scripts and statements
  Data editing form table viewer by using "Edit" button at each row
  New records can be added by using the "New" button in the table heading
    Input mode during editing and insert depends on the fields data type. SQLite
	uses a dynamic type system where field types can be given as any string
	aSQLiteManager translates this string into the following types and use this
	to set input mode (SQLite types i parenthesis). The translation is NOT
	case sensitive:
		STRING (string, text), INTEGER (integer), FLOAT (real, float, double),
		DATE (date), TIME (time), DATETIME (datetime), BOOLEAN (boolean, bool),
		PHONENO (phoneno). aSQLiteManager does not handle BLOB

Version 1.2β
  Open SQL files (from database view) and executed all or single lines
  Error messages during restore and execution of scripts and statements
  Data editing form table viewer by using "Edit" button at each row
  New records can be added by using the "New" button in the table heading
    Input mode during editing and insert depends on the fields data type. SQLite
	uses a dynamic type system where field types can be given as any string
	aSQLiteManager translates this string into the following types and use this
	to set input mode (SQLite types i parenthesis). The translation is NOT
	case sensitive:
		STRING (string, text), INTEGER (integer), FLOAT (real, float, double),
		DATE (date), TIME (time), DATETIME (datetime), BOOLEAN (boolean, bool),
		PHONENO (phoneno). aSQLiteManager does not handle BLOB

Version 1.1
  Select SQL history from query form
  Size of icons changed to right sizes for different displays
  Better detection of valid SQLite databases (based on first 16-byte sequence)
  Begin transaction, Commit and Rollback can be selected from option menu. 
    Transactions can be nested as savepoints in SQLite (is the first transaction
    rolled back all updates are rolled back). aSQLiteManager ensures that SQL
    statements are stored in the database even if updates are rolled back.
    See http://www.sqlite.org/lang_savepoint.html for savepoint specifications.
  Export of databases (table structure, views and data) to SQL script
    named databaseFileName.sql implemented -- seems to work
  Restore of databases (table structure, views and data) from SQL script
    named databaseFileName.sql implemented -- seems to work
  Better file picker with different icons for folders, .sqlite, .db and non
  	aSQLiteManager files
  Export of query result to ascii file databaseFileName.export
  Configuration of:
    Open databases from file managers and aSQLiteManager without question
    Reset configurations to default
    
Version 1.1β
  Select SQL history from query form
  Size of icons changed to right sizes for different displays
  Better detection of valid SQLite databases (based on first 16-byte sequence)
  Begin transaction, Commit and Rollback can be selected from option menu. 
    Transactions can be nested as savepoints in SQLite (is the first transaction
    rolled back all updates are rolled back). aSQLiteManager ensures that SQL
    statements are stored in the database even if updates are rolled back.
    See http://www.sqlite.org/lang_savepoint.html for savepoint specifications.
  Export of databases (table structure, views and data) to SQL script
    named databaseFileName.sql implemented -- seems to work
  Restore of databases (table structure, views and data) from SQL script
    named databaseFileName.sql implemented -- seems to work
  Better file picker with different icons for folders, .sqlite, .db and non
  	aSQLiteManager files
  Export of query result to ascii file databaseFileName.export
  
Version 1.0
  First production release of aSQLiteManager
  No known bugs
  
Version 0.81
  aSQLiteManager can be opened if a .sqlite or .db file is clicked on in
    OI File Manager, in Adao Teams File Manager and in Simplest File Manager
  Insert into SQL builder mode
  A few bug fixed

Version 0.80
  The first beta version of aSQLiteManager
  More advanced SQL builder (for Select, Create view, Create table, Drop table,
    Drop view and Delete from)
  The sqlite_master table now included in list of tables and sorted by name
  Create new database and open it
  Headers on query results
  Don't save the same SQL more than one time (unique constraints on SQL)
  Index viewer

Version 0.70
  Query result in data grid
  Executed statements can be saved in current database in a table named
    aSQLiteManager (configuration)
  A simple SQL builder where tables and fields from the "select fields from tables"
    part can be selected from two lists
  Paging on Table and View data viewer implemented. Numbers of records to return can be
    configured form option menu at the main form
  Clicking on a field in the data browser copy the content to the clip board
  A help screen with a little help, links and mailto
  
Version 0.6
  Simple execution of SQL and display of result (for the time being as comma
  separated lines in list)
  
Version 0.50
  Display view data
  Display create script for tables and views
  Enhanced file picker (sorted list - directories before files and icons)
  Two way scrolling data grid
  Can be moved to SDCard
  
Later:
  See triggers

Version 0.11

New in this version:
  Can now display table data

Version 0.1

Now able to:
  Browse SDCard and open a database
  See tables, views and index
  See table structure
