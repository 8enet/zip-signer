INTRO

zipsigner-lib is a open source Java API for signing Zip, Apk, and Jar
files.  It was developed for use onboard Android devices, but will
work to sign files in other environments that support the Java
programming language.

The source to this library is licensed under Apache 2.0.

The primary class is kellinwood.security.zipsigner.ZipSigner.  It is
is a heavily modified version of Google's SignApk. Modifications
include the addition of convienience methods, a progress listener API,
default keys/certificates built into the classpath, and a signature
implementation that generates signatures recognized by the "install
update.zip file" feature of the recovery programs.

DEPENDENCIES

This project currently depends on other libraries:

- zipio-lib, an alternate API to java.util.zip.* for reading and
  writing zip files.  This library allows entries to be copied
  directly from the input to output without de-compressing and 
  re-compressing.  It also zip-aligns to 4 byte boundaries by default.
  
- kellinwood-logging-lib, a small platform-independent logging
  framework.  In order to troubleshoot during development I needed to
  have zipsigner-lib run on both Android and my desktop JRE so I could
  compare results.  This meant having a portable logging API but I
  couldn't figure out how to make java.util.logging work on Android so
  I wrote this. The source to this library is licensed under Apache 2.0.

- kellinwood-logging-android, the android adapter for
  kellinwood-logging-lib.  This is not required, but if you want to
  see any loggging output from zipsigner-lib on Android you'll need to
  include this library and activate it via a few API calls (see below).
  The source to this library is licensed under Apache 2.0.

- OPTIONAL: android-sun-jarsign-support, which contains the Sun code
  required to create a properly formated PKCS#7 signature block file.
  This dependency is only needed if you are not using the default
  certificate and have not supplied a signature block template file
  (see below for more information on this subject). The code in this
  library was obtained from the OpenJDK project and is licensed under
  GPL version 2.

- For use in normal desktop JRE applications, you must install Bouncy 
  Castle Crypto API.  http://www.bouncycastle.org/java.html.  The BC 
  cryto provider is the default on Android, but not in other environments
  that support the Java programming language.

SOURCE

All source is 100% Java and there are no dependencies on other
installable components such as busybox, openssl, etc.  Root privileges
are not required but you probably need root in order to do something
meaningful with the results.  To use this library you'll probably
need to give your app write privileges to the sdcard.

For a demonstration of this API in use, please refer to the source
code of the ZipSigner app.

All code is available from http://code.google.com/p/zip-signer.  This
includes the above mentioned dependencies, this project's code, and
the ZipSigner Android app.

BASIC USAGE:

import kellinwood.security.zipsigner.ZipSigner;

try {
    // Sign with the built-in default test key/certificate.
    ZipSigner zipSigner = new ZipSigner();
    zipSigner.signZip( inputFile, outputFile);
}
catch (Throwable t) {
    // log, display toast, etc.
}

KEYS/MODES

ZipSigner can sign with the four Google keys: "media", "platform",
"shared", and "testkey".  There are also two auto-key modes -- "auto"
and "auto-testkey". In auto mode, ZipSigner examines the signature
block of the input file and automatically determines which key should
be used to sign the file such that the output is signed with the same
key as the input file.  In "auto" mode the signing operation will fail
if it can't determine which of the four keys to use.  The mode
"auto-testkey" is similar except ZipSigner falls back to the testkey
if it cant automatically determine which key to use.  Specifying one
of the keys directly as the key/mode will force the output to be
signed with the specified key, regardless of which key the intput file
was signed with.

zipSigner.setKeyMode( String mode)

GETTING PROGRESS UPDATES:

import kellinwood.security.zipsigner.ProgressListener;
import kellinwood.security.zipsigner.ProgressEvent;

ZipSigner zipSigner = new ZipSigner();
zipSigner.addProgressListener( new ProgressListener() {
   public void onProgress( ProgressEvent event)
   {
      String message = event.getMessage();
      int percentDone = event.getPercentDone();
      // log output or update the display here       
   }
});
zipSigner.signZip( inputFile, outputFile);


ENABLING LOG OUTPUT:

import kellinwood.logging.LoggerManager;
import kellinwood.logging.android.AndroidLogger;
import kellinwood.logging.android.AndroidLoggerFactory;

// In Activity.onCreate()...
LoggerManager.setLoggerFactory( new AndroidLoggerFactory());

// Optional, use this logging API in your own code
AndroidLogger logger = (AndroidLogger)LoggerManager.getLogger(this.getClass().getName());

// Optional, enable toasts.  If enabled, they are shown by default for
// error and warning level log output.
logger.setToastContext(getBaseContext());

// Maybe also show toasts for debug output.
// logger.setDebugToastEnabled(true);

// Optional, log something
logger.debug("Hello, world!");


Use the following adb commands to enable logcat output from zipsigner-lib:

adb shell setprop log.tag.ZipSigner VERBOSE
adb logcat ZipSigner:*


SIGNING WITH OTHER CERTIFICATES

    // Load an x509.pem public key certificate.  E.g., "file:///sdcard/mycert.x509.pem"
    public X509Certificate readPublicKey(URL publicKeyUrl);

    // Load a pkcs8 encoded private key.  Password is only required if the key is encrypted.
    public PrivateKey readPrivateKey(URL privateKeyUrl, String keyPassword);
        
    // Fetch the content at the specified URL and return it as a byte array.
    // Use this method to load signature block template files.  
    public byte[] readContentAsBytes( URL contentUrl);

    // Sign the zip using the given public/private key pair. Signature block template may be null if 
    // you've included android-sun-jarsign-support.jar in the build.
    public void setKeys( X509Certificate publicKey, PrivateKey privateKey, byte[] signatureBlockTemplate);

    // Sign the zip using a cert/key pair from the given keystore.  Keystore type on Android is "BKS".
    // See below for information on creating an Android compatible keystore.
    public void signZip( URL keystoreURL, 
                         String keystoreType,
                         String keystorePw, 
                         String certAlias,
                         String certPw, 
                         String inputZipFilename, 
                         String outputZipFilename);


KEYSTORE CREATION

An Android compatible keystore can be created on your desktop system.
Here are some brief instructions:

* Download Bouncy Castle Provider, e.g., bcprov-jdk16-145.jar, from
  http://www.bouncycastle.org/latest_releases.html

* Copy bcprov-jdk16-145.jar to $JDK_HOME/jre/lib/ext/.

* Create the key...

keytool -genkey \
        -alias CERT \
        -keystore assets/keystore.ks \
        -storetype BKS \
        -provider org.bouncycastle.jce.provider.BouncyCastleProvider \
        -storepass android \
        -keyalg RSA \
        -keysize 1024 \
        -keypass android \
        -sigalg SHA1withRSA \
        -dname "C=US,ST=California,L=Mountain View,O=Android,OU=Android,CN=Android"

* Create the cert...

keytool -selfcert -validity 9125 -alias CERT \
    -keystore assets/keystore.ks \
    -storepass android -keypass android \
    -storetype BKS \
    -provider org.bouncycastle.jce.provider.BouncyCastleProvider


* List the contents of the keystore:

keytool -list -v -keystore assets/keystore.ks \
    -storepass android -keypass android -storetype BKS \
    -provider org.bouncycastle.jce.provider.BouncyCastleProvider

* When signing files using zipsigner-lib or the ZipSigner app and you
  get and "UnrecoverableKeyException: no match" error message, it
  means that you are providing a bad key password.


SIGNATURE BLOCK TEMPLATE

The signature block file, CERT.RSA, contains PCKS#7 formatted data.
In the initial version of zipsigner-lib, the code in
sun.security.x509, sun.security.pcks, etc, was used to write this data
structure.  The only inputs are the x509 public key certificate and
the signature bytes.  Luckily for us, the signature bytes are at the
very end of the PCKS#7 block, making it easy to create a PCKS#7
template based on the certificate and just append the signature bytes.
This technique eliminates the dependency on a large part of the Sun
code, but it means that a signature block template must be created for
the certificate.

Note that zipsigner-lib contains and loads a signature block template
when using the default key and certificate.

Step 1, sign a file using the certificate for which the template is
needed.  Use the desktop command line version of zipsigner and give it
the -d option so that you can see the signature data.

Step 2, determine where the signature data begins in the signature
block file (CERT.RSA).  This is typically at byte 1458, or address
0x5b2.

E.g., "unzip -c test_signed.zip META-INF/CERT.RSA | hexdump -C"

Step 3, create the signature block template:

unzip -qc test_signed.zip META-INF/CERT.RSA | dd bs=1458 count=1 >testkey.sbt
