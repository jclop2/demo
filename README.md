# A usage example of JClop with Dropbox

It shows how to use [JClop](https://github.com/jclop2/JClop) to open a file selection dialog that allows you to read/write files from/to your local drive or Dropbox.

## How to use it

### Prerequisites
- This demo requires Java6+ to be installed.
- To be able to connect to [Dropbox](https://www.dropbox.com), an application needs to have an application key and a secret key.
  Please have a look at [https://www.dropbox.com/developers](https://www.dropbox.com/developers) in order to know how to obtain this data.

### Make it run
1. Download the sources in your favorite development environment.
2. Edit the ```src/main/resources/com/fathzer/soft/jclop/demo/keys.properties``` file, to add your application key and secret key.
3. Compile the source with your development environment (or with Maven: ```mvn clean compile```).
4. Launch the com.fathzer.soft.jclop.demo.Test class

### How to use it
The buttons "Select for Reading" and "Select for writing" display an extended (by the ability to choose file in Dropbox) file selection dialog respectively for reading/writing a file.

The first time you choose the Dropbox tab, you'll have to login to your Dropbox account.  
Please follow the process described in the dialog. Your credentials will then be stored in the *cache* directory.

Once you have selected a file, its URL is displayed at the top of the screen.

You can then click the *Read* or *Write* buttons to read/write to/from this file.


### Perform some cleaning
JClop uses a cache to keep your credentials and copies of files accessed in your Dropbox account.  
It could be wise to delete this directory after usage (folder *cache* in the project directory).

