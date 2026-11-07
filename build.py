#!/usr/bin/env python3
"""Build with Python 3 and Java 17; downloads pinned build dependencies."""
import argparse, os, pathlib, re, shutil, subprocess, urllib.request, zipfile
parser=argparse.ArgumentParser()
mode=parser.add_mutually_exclusive_group()
mode.add_argument("--unsigned",action="store_true",help="Compile without signing")
mode.add_argument("--debug",action="store_true",help="Use the public development certificate")
args=parser.parse_args()
ROOT=pathlib.Path(__file__).resolve().parent
VERSION=re.search(r"versionName:\s*(\S+)",(ROOT/'package/apktool.yml').read_text()).group(1)
if not args.unsigned and not args.debug:
 if not os.environ.get('FLOW_KEYSTORE') or not os.environ.get('FLOW_KEYSTORE_PASSWORD_FILE'):
  parser.error('Release signing needs FLOW_KEYSTORE and FLOW_KEYSTORE_PASSWORD_FILE; use --unsigned or --debug for development')
TOOLS=ROOT/'tools';TOOLS.mkdir(exist_ok=True)
URLS={
 'android.jar':'https://repo.maven.apache.org/maven2/com/google/android/android/4.1.1.4/android-4.1.1.4.jar',
 'xposed.aar':'https://repo.maven.apache.org/maven2/io/github/libxposed/api/102.0.0/api-102.0.0.aar',
 'r8.jar':'https://dl.google.com/dl/android/maven2/com/android/tools/r8/8.7.18/r8-8.7.18.jar',
 'apktool.jar':'https://github.com/iBotPeaches/Apktool/releases/download/v2.12.1/apktool_2.12.1.jar',
 'signer.jar':'https://github.com/patrickfav/uber-apk-signer/releases/download/v1.3.0/uber-apk-signer-1.3.0.jar'}
for n,url in URLS.items():
 p=TOOLS/n
 if not p.exists():
  print('Downloading',n,flush=True)
  p.write_bytes(urllib.request.urlopen(url,timeout=120).read())
with zipfile.ZipFile(TOOLS/'xposed.aar') as z:(TOOLS/'xposed.jar').write_bytes(z.read('classes.jar'))
classes=ROOT/'classes';classes.mkdir(exist_ok=True)
cp=str(TOOLS/'android.jar')+':'+str(TOOLS/'xposed.jar')
def run(*args):subprocess.run([str(x) for x in args],cwd=ROOT,check=True)
run('java',ROOT/'Compile.java','-source','8','-target','8','-encoding','UTF-8','-cp',cp,'-d',classes,*sorted((ROOT/'src').rglob('*.java')))
run('java','--class-path',classes,ROOT/'tests/PaletteTest.java')
run('java','--class-path',classes,ROOT/'tests/GlassPolicyTest.java')
run('java','--class-path',classes,ROOT/'tests/RenderSessionTest.java')
run('java','--class-path',classes,ROOT/'tests/FooterGradientRegistryTest.java')
run('java','--class-path',classes,ROOT/'tests/FooterFrostedGeometryTest.java')
with zipfile.ZipFile(ROOT/'classes.jar','w') as z:
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes))
run('java','-cp',TOOLS/'r8.jar','com.android.tools.r8.D8','--min-api','32','--lib',TOOLS/'android.jar','--classpath',TOOLS/'xposed.jar','--output',ROOT/'package',ROOT/'classes.jar')
assets=ROOT/'package/assets';assets.mkdir(exist_ok=True)
with zipfile.ZipFile(assets/'module-source.zip','w',zipfile.ZIP_DEFLATED) as z:
 selected=[ROOT/n for n in ['Compile.java','build.py','README.md','CHANGELOG.md','LICENSE','.gitignore','package/AndroidManifest.xml','package/apktool.yml']]
 for folder in ['src','tests','docs','package/res','package/unknown']:
  selected.extend(p for p in (ROOT/folder).rglob('*') if p.is_file())
 for p in sorted(set(selected)):
  if p.is_file():z.write(p,p.relative_to(ROOT))
run('java','-jar',TOOLS/'apktool.jar','b',ROOT/'package','-o',ROOT/'unsigned.apk')
if args.unsigned:
 print('Unsigned APK:',ROOT/'unsigned.apk')
 raise SystemExit(0)
signed_dir=ROOT/'dist'/VERSION
signed_dir.mkdir(parents=True,exist_ok=True)
for p in signed_dir.glob('unsigned-aligned-*.apk*'):p.unlink()
sign=['java','-jar',str(TOOLS/'signer.jar'),'-a',str(ROOT/'unsigned.apk'),'-o',str(signed_dir)]
if not args.debug:
 password=pathlib.Path(os.environ['FLOW_KEYSTORE_PASSWORD_FILE']).read_text().strip()
 sign.extend(['--ks',os.environ['FLOW_KEYSTORE'],'--ksAlias',os.environ.get('FLOW_KEY_ALIAS','chatgptflow'),'--ksPass',password,'--ksKeyPass',password])
result=subprocess.run(sign,cwd=ROOT)
if result.returncode:raise SystemExit('APK signing failed')
candidates=list(signed_dir.glob('unsigned-aligned-*.apk'))
if len(candidates)!=1:raise SystemExit('Missing unique signed APK')
# Verify the container before copying a signer output to the deliverable name.
with zipfile.ZipFile(candidates[0]) as z:
 if z.testzip() is not None:raise SystemExit('APK integrity check failed')
output=signed_dir/('ChatGPT-Flow-'+VERSION+('-debug' if args.debug else '')+'.apk')
shutil.copyfile(candidates[0],output)
print('Signed APK:',output)
