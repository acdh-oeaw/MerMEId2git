package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import at.ac.oeaw.acdh.bruckneronline.mermeid2git.CommitProposal.InitialState;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.db.ExistdbClientCallback;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.db.ExistdbClientException;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.db.MermeidExistdbClient;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.db.MermeidExistdbConnectionInfo;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui.MainWindow;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui.SettingsWindow;

/**
 * 
 * @author mcupak
 *
 */
public class App {
	public static final String TITLE, VERSION;
	static {
		String title = App.class.getPackage().getImplementationTitle();
		TITLE = title == null ? "merMEId2git" : title;
		
		String version = App.class.getPackage().getImplementationVersion();
		VERSION = version == null ? "DEV" : version;
	}

	private static App instance = null;

	public final at.ac.oeaw.acdh.bruckneronline.mermeid2git.Logger lgr = new Logger();
	
	private final SettingsXml settings;
	private final List<AppCallback> callbacks;
	private final DocumentBuilder documentBuilder;
	private final XPathExpression xpeWab, xpeTitle, xpeLastChangeDescription, xpeResponsible;
	private final Map<String, Commiter> commiters;

	private MermeidExistdbClient dbClient;
	
	private Git gitRepo;
	private UsernamePasswordCredentialsProvider gitCredsProvider;
	
	private volatile boolean guiInit = false;
	
	public static synchronized App getInstance() {
		if (instance == null) {
			instance = new App();
		}
		return instance;
	}
	
	public App() {
		callbacks = new ArrayList<>();
		
		final File execDir = getExecutionDir();
		
		// init logging
		lgr.logInto(new File(execDir, "log.txt"), 0, 5, false);	// keep logs from last 5 runs, one log file per run
		lgr.info(TITLE + " v" + VERSION);
		
		// init settings file
		settings = new SettingsXml();
		File settingsXml = new File(execDir, "settings.xml");
		lgr.info("using settings file '" + settingsXml.getAbsolutePath() + "'");

		DocumentBuilder tmpDocBuilder = null;
		XPathExpression tmpXpeWab = null, tmpXpeTitle = null, tmpXpeLastChangeDescription = null, tmpXpeResponsible = null;
		Map<String, Commiter> tmpCommiters = null;

		try {
			if (settingsXml.exists()
					&& settingsXml.length() == 0
					&& !settingsXml.delete()) {
				throw new ExitException("unable to delete empty file '" + settingsXml.getAbsolutePath() + "'", ExitCode.EXCEPTION_WHILE_SAVING_DEFAULT_SETTINGS);
			}
			
			if (!settingsXml.exists()) {
				
				// output defaults
				settings.set("db.user", "");
				settings.set("db.password", "");	// TODO: plain?
				settings.set("db.uri", "xmldb:exist://");
				settings.set("db.collection", "dcm");
				settings.setBool("db.ssl", true);
				settings.setFile("git.repo.localdir", new File(System.getProperty("user.dir")));
				settings.set("git.user", System.getProperty("user.name"));
				settings.set("git.password", "");	// TODO: plain?
				settings.set("git.committers", "User Name1 <user.name1@sample.domain> autoinclude\nUser Name2 <user.name2@sample.domain>\nUser Name3 <user.name3@sample.domain>");

				try {
					if (!settingsXml.createNewFile()) {
						throw new IOException("unable to create default " + settingsXml.getName() + " file");
					}
					
					new SettingsWindow(null, settings);
					
					settings.save(settingsXml, SettingsXml.getDefaultComment());
					
				} catch (IOException ioe) {
					throw new ExitException("exception while creating default " + settingsXml.getName() + " file", ExitCode.EXCEPTION_WHILE_SAVING_DEFAULT_SETTINGS, ioe);
				}
				
			} else {
				// load existing settings
				try {
					settings.load(settingsXml);
					
				} catch (IOException ioe) {
					throw new ExitException("exception while loading " + settingsXml.getName() + " file", ExitCode.EXCEPTION_LOADING_SETTINGS, ioe);
				}
			}
			
			// init XML parsing
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			try {
				tmpDocBuilder = factory.newDocumentBuilder();
				tmpXpeWab = xpath.compile("/mei/meiHead/workDesc/work/identifier[@label='WAB']/text()");
				tmpXpeTitle = xpath.compile("/mei/meiHead/fileDesc/titleStmt/title/text()");
				tmpXpeLastChangeDescription = xpath.compile("/mei/meiHead/revisionDesc/change[last()]/changeDesc/p/text()");
				tmpXpeResponsible = xpath.compile("/mei/meiHead/revisionDesc/change[last()]/respStmt/resp/text()");
				
			} catch (ParserConfigurationException | XPathExpressionException e) {
				throw new ExitException("exception while initialisig XML parsing", ExitCode.EXCEPTION_WHILE_XML_PARSING_INIT, e);
			}
			
			// init commiters-map
			tmpCommiters = new HashMap<>();
			final String strCommiters = settings.get("git.committers");
			if (strCommiters == null) {
				throw new ExitException("no commiters defined in settings INI", ExitCode.NO_COMMITTERS_DEFINED);
			}
			final String[] strSplitCommiters = strCommiters.split("\n");
			if (strSplitCommiters.length < 1) {
				throw new ExitException("there must be at least one commiter defined in settings INI", ExitCode.NO_COMMITTERS_DEFINED);
			}
			for (String config : strSplitCommiters) {
				Commiter c = new Commiter(config);
				tmpCommiters.put(c.personIdent.getName(), c);
			}
		
		} catch (ExitException aee) {
			lgr.log(Level.SEVERE, aee.getMessage(), aee.getCause());
			exit(aee.exitCode);
		}
		
		documentBuilder = tmpDocBuilder;
		xpeWab = tmpXpeWab;
		xpeTitle = tmpXpeTitle;
		xpeLastChangeDescription = tmpXpeLastChangeDescription;
		xpeResponsible = tmpXpeResponsible;
		commiters = tmpCommiters;
	}
	
	public void addCallback(AppCallback ac) {
		callbacks.add(ac);
	}
	
	/**
	 * <p>Connects to databse and git repository.</p>
	 * 
	 * @throws ExistdbClientException
	 * @throws IOException
	 */
	public synchronized void connect() throws ExistdbClientException, IOException {
		File repoDir = new File(settings.get("git.repo.localdir"));

		// init DB connection
		if (dbClient == null) {
			lgr.info("opening database connection...");
			
			MermeidExistdbConnectionInfo meci = new MermeidExistdbConnectionInfo(
					settings.get("db.uri"),
					settings.get("db.user"),
					settings.get("db.password"),
					settings.get("db.collection"),
					settings.getBool("db.ssl"));
			
			dbClient = new MermeidExistdbClient(meci, repoDir);
			
			dbClient.addCallback(new ExistdbClientCallback() {
				@Override
				public void skippingResource(String resourceName) {
					lgr.info("skipping resource '" + resourceName + "'");
				}
				
				@Override
				public void exceptionOccured(ExistdbClientException ece) {
					lgr.log(Level.WARNING, "exception while downloading", ece);
				}
				
				@Override
				public void downloadStarted(String resourceName) {
					lgr.info("downloading resource '" + resourceName + "'");
				}
				
				@Override
				public void downloadFinished(File f, long duration) {
					lgr.info("file '" + f.getAbsolutePath() + "' downloaded in " + duration + " [ms]");
				}

				@Override
				public void downloadFinished(String collectionName, int resourceCount, long duration) {
					lgr.info("collection '" + collectionName + "' with " + resourceCount + " resources downloaded in " + duration + " [ms]");
					for (AppCallback ac : callbacks) {
						ac.downloadDone();
					}
				}
			});
			
			lgr.info("...connected");
			
		} else {
			lgr.info("database already connected");
		}

		// init git repo
		if (gitRepo == null) {
			lgr.info("opening git repository connection...");
			gitRepo = Git.open(repoDir);
			gitCredsProvider = new UsernamePasswordCredentialsProvider(settings.get("git.user"), settings.get("git.password"));
			
			lgr.info("...connected");
			
		} else {
			lgr.info("git repository already connected");
		}
	}
	
	public synchronized void download() throws ExistdbClientException, GitAPIException {
		dbClient.download(null, null);
	}
	
	public synchronized void getChanges() throws GitAPIException {
		StatusCommand sc = gitRepo.status();
		Status s = sc.call();
		
		for (String uncommitedChange : s.getUncommittedChanges()) {
			getChanges(uncommitedChange, InitialState.UNCOMMITED);
		}
		for (String untrackedChange : s.getUntracked()) {
			getChanges(untrackedChange, InitialState.UNTRACKED);
		}
	}
	
	private void getChanges(String fileName, InitialState initialState) {
		for (AppCallback ac : callbacks) {
			File repoBase = settings.getFile("git.repo.localdir");
			File f = new File(repoBase, fileName);
			CommitProposal cp = null;
			try {
				cp = createCommitProposal(initialState, f);
				
			} catch (IOException | SAXException e) {
				lgr.log(Level.SEVERE, "unable to parse file '" + f.getAbsolutePath() + "'", e);
				continue;
			}
			ac.fileChangeDetected(cp);
		}
	}
	
	public synchronized void checkout(String filePathRelativeFromGitBase) throws GitAPIException {
		CheckoutCommand chc = gitRepo.checkout();
		chc.addPath(filePathRelativeFromGitBase);
		chc.setCreateBranch(false);
		chc.call();
		lgr.info("checkout succeeded: " + filePathRelativeFromGitBase);
	}
	
	public synchronized void checkoutAll() throws GitAPIException {
		CheckoutCommand cc = gitRepo.checkout();
		cc.setAllPaths(true);
		cc.setCreateBranch(false);
		cc.call();
		lgr.info("checkout-all succeeded");
	}
	
	public synchronized void pull() throws GitAPIException {
		PullCommand pc = gitRepo.pull();
		pc.setCredentialsProvider(gitCredsProvider);
		pc.call();
		lgr.info("pull succeeded");
	}
	
	public synchronized void commit(CommitProposal cp) throws GitAPIException {
		if (cp.isSkip()) {
			lgr.info("skipping commit [raised skip-flag], " + cp);
			return;
		}
		
		String commitMessage = cp.getCommitMessage();
		if (commitMessage == null
				|| commitMessage.length() == 0) {
			lgr.warning("skipping commit [empty commit message], " + cp);
			return;
		}
		
		Commiter commiter = cp.getCommiter();
		if (commiter == null) {
			lgr.warning("skipping commit [undefined commiter], " + cp);
			return;
		}
		
		String filePathRelativeFromGitBase = cp.file.getAbsolutePath().substring((settings.get("git.repo.localdir").length() + 1));
		filePathRelativeFromGitBase = filePathRelativeFromGitBase.replace('\\', '/');	// Windows
		
		if (cp.initialState == InitialState.UNTRACKED) {
			AddCommand ac = gitRepo.add();
			ac.addFilepattern(filePathRelativeFromGitBase);
			ac.call();
		}
		
		CommitCommand cc = gitRepo.commit();
		cc.setOnly(filePathRelativeFromGitBase);
		cc.setMessage(commitMessage);
		cc.setAuthor(commiter.personIdent);
		cc.setCommitter(commiter.personIdent);
		try {
			cc.call();
			
		} catch (JGitInternalException jgie) {
			if ("No changes".equals(jgie.getMessage())) {
				lgr.info("commit skipped due to no changes, " + cp + ", trying to checkout the file");
				
				checkout(filePathRelativeFromGitBase);
				
			} else {
				throw jgie;
			}
		}
		
		lgr.info("commit done, " + cp);
	}
	
	public Collection<Commiter> getCommiters() {
		return commiters.values();
	}
	
	public synchronized void push() throws GitAPIException {
		PushCommand pc = gitRepo.push();
		pc.setCredentialsProvider(gitCredsProvider);
		pc.call();
		lgr.info("push succeeded");
	}
	
	public synchronized void invokeGui() {
		if (!guiInit) {
			guiInit = true;
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new MainWindow();
				}
			});
			
		} else {
			lgr.warning("no multiple instances of MainWindow are permitted");
		}
	}
	
	public SettingsXml getSettings() {
		return settings;
	}
	
	public void exit(ExitCode ec) {
		if (settings != null) {
			try {
				settings.save(SettingsXml.getDefaultComment());
				
			} catch (IOException ioe) {
				lgr.log(Level.WARNING, "exception while saving settings", ioe);
			}
		}
		
		if (dbClient != null) {
			try {
				dbClient.close();
				
			} catch (ExistdbClientException dce) {
				lgr.log(Level.WARNING, "exception while closing database client", dce);
			}
		}
		
		if (gitRepo != null) {
			gitRepo.close();
		}
		
		lgr.info("closed resources, closing logger and exiting with status " + ec);
		lgr.close();
		
		System.exit(ec.intCode);
	}
	
	private CommitProposal createCommitProposal(InitialState initialState, File f) throws SAXException, IOException {
		String strWab = null, strTitle = null, strLastCommitMessage = null, strResponsible = null;
		Commiter commiter = null;

		if (f.getName().toLowerCase().endsWith(".xml")) {
			Document doc = documentBuilder.parse(f);
			
			try {
				strWab = (String) xpeWab.evaluate(doc, XPathConstants.STRING);
				strTitle = (String) xpeTitle.evaluate(doc, XPathConstants.STRING);
				strLastCommitMessage = (String) xpeLastChangeDescription.evaluate(doc, XPathConstants.STRING);
				strResponsible = (String) xpeResponsible.evaluate(doc, XPathConstants.STRING);
				
			} catch (XPathExpressionException e) {
				// XPaths are hard-coded, so this should never occur
				lgr.log(Level.SEVERE, "invalid XPath expression", e);
				exit(ExitCode.EXCEPTION_INVALID_XPATH_EXPRESSION);
			}
		}

		if (strResponsible != null) {
			commiter = commiters.get(strResponsible);
		}

		final CommitProposal cp = new CommitProposal(initialState, f, strWab, strTitle, strResponsible, commiter);
		cp.setCommitMessage(strLastCommitMessage);
		cp.setSkip(commiter == null ? true : !commiter.autoInclude);
		
		return cp;
	}
    
    private File getExecutionDir() {	// TODO : refactor to helpers
    	ProtectionDomain pd = App.class.getProtectionDomain();
    	CodeSource cs = pd.getCodeSource();
    	URL url = cs.getLocation();
    	URI uri = null;
		try {
			uri = url.toURI();
			
		} catch (URISyntaxException e) {
			lgr.log(Level.WARNING, "cannot get execution directory", e);
			exit(ExitCode.EXCEPTION_GETTING_EXECUTION_DIRECTORY);
		}
		File jar = new File(uri);
		return jar.getParentFile();
    }
	
    public static void main(String[] args) throws InterruptedException {
		App.getInstance().invokeGui();
    }
}
