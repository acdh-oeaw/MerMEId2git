# MerMEId2git

Java Swing application facilitating [MerMEId](https://github.com/Det-Kongelige-Bibliotek/MerMEId) content download and git upload.

It is developed at the [Austrian Centre for Digital Humanities](https://www.oeaw.ac.at/acdh/acdh-home/) (ACDH) and used by the [Institut für Kunst- und Musikhistorische Forschungen](https://www.oeaw.ac.at/ikm/home/) (IKM) to conveniently backup MerMEId content of [Digitales Werkverzeichnis Anton Bruckner](http://www.bruckner-online.at/) into git repositories and track MerMEId metadata history by reflecting it in git commit messages.

## Installation

* JRE 8 or higher version is required to run.
* Clone the repository using `git clone https://github.com/acdh-oeaw/MerMEId2git.git` command.
* There is pre-compiled JAR file, which you can run by `java -jar trunk/target/mermeid2git.jar` command.
* The JAR file can be copied into any folder, configuration file `settings.ini` with default values will be created as sibling to JAR file, in the same parent directory into which you place the JAR.

## Configuration

* Upon first start (creation of `settings.ini` file described in previous step) a GUI window will pop up.
* Following parameters are needed to be set:
  * `DB URI` - eXist database URI, prefixed with `xmldb:exist://`
  * `DB user`, `DB password` - credentials to log into the eXist database. The password is stored locally.
  * `DB MerMEId collection` - pre-configured constant `dcm` will most likely not needed to be changed. It is name of eXist-db collection where MerMEI'd data XMLs reside.
  * `DB use SSL` - Java client connecting to eXist usually needs to use Secure Sockets Layer
  * `Git repository directory` - local or network path to store the XML downloads from MerMEId's collection. Although possible, it isn't recommended to use network path because of performance issues. Remember that the local data are only temporary and network storage would get used in the same manner.
  * `Git user`, `Git password` - credentials to pull from and push into the above mentioned git repository. The password is stored locally.
  * `Git commiters` - input line in format `User Name <user.name@sample.domain>`. These option will allow selection of commiter(s) later on. It is possible to raise the `autoinclude` flag on the end of such line `User Name <user.name@sample.domain> autoinclude`, which will include all rows into the commit automatically, if the extracted responsible person's name will match with commiter defined by this option.

## Usage

* After initial configuration and upon every application startup, database and git repository connection will be opened automatically.
* The usual workflow:
  * Click "Download & report changes".
  * After the download will finish, XMLs will be parsed and commit messages pre-filled, if the data contain appropriate descriptions in file history.
  * Based upon the `autoinclude` configuration option in the "Git commiters", rows to commit will get pre-checked.
  * You have the option to manually edit commit messages, change commiter or manually include/exclude a row to/from a commit.
  * Click "Commit & push" to upload. One commit per one row has to occur, as each row has its own commit message. After all commits are done, a single push will follow.
  * If the commit message was left empty, or for some other reason commit could not be done, the row will reappear.
* By clicking column names in table's header it is possible to change table sorting parameter.
* Repeated clicking on the same column name results to switching between ascending and descending order.
* It is possible to change column widths.
* Sorting selection and state of the window (column widths, splitter position, size, location) will be saved in the configuration file and re-loaded during the very next startup.
* Left-click the table content to pop up context menu. It allows you to:
  * Include or exclude all rows into a commit.
  * Set value of "Commit as" column in all rows.

## Known problems / TODOs

* Use sophisticated password storage. (Users tend to have their home-folders not encrypted.)
* It should not be allowed to drag columns in the results table.
* JGitInternalException: Entry not found by path - when using relative paths, like for example `../../../local-git-repo`, try to query the Git object instance for its root.
* JTextArea looses auto-scroll on focus gain or text-select when scrolling. The auto-scroll isn't restored on focus lost or text-deselect.