; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "SDFManager"
#define MyAppVersion "4.3.6"
#define MyAppPublisher "EEA"
#define MyAppURL "http://bd.eionet.europa.eu/activities/Natura_2000/index_html"
#define MyAppExeName "SDFManager.exe"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
; AppId={{872F6301-DCAB-4E5D-A7BC-BF97036EAF56} 
AppId={code:GetAppID}

AppName={code:GetAppName}
AppVersion={#MyAppVersion}
VersionInfoVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={userdocs}\{code:GetAppName}
DefaultGroupName={code:GetAppName}
DisableDirPage=no
AllowNoIcons=yes
OutputDir=..\
OutputBaseFilename=SDFManagerSetup_v{#MyAppVersion}
SetupIconFile=sdfmanager.ico
Compression=lzma
SolidCompression=yes
UsePreviousLanguage=no

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

;Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "SDFManagerEmerald"; Check: IsEmeraldMode
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 0,6.1


[Code]
var
  IsEmerald: Boolean;
  IsNatura2000: Boolean;       
  Page:    TWizardPage;
  Natura2000Radio, EmeraldRadio: TNewRadioButton;

  

procedure InitializeWizard;
var
 
  Natura2000Image, EmeraldImage:  TBitmapImage;
  Natura2000BMPFileName, EmeraldBMPFileName: String;
     
begin
  Page := CreateCustomPage(wpWelcome, 'Application Mode', 'Please select In which network the SDFManager will be used');

  Natura2000BMPFileName := ExpandConstant('{tmp}\natura2000_logo_small.bmp'); 
  ExtractTemporaryFile(ExtractFileName(Natura2000BMPFileName));
  Natura2000Image := TBitmapImage.Create(Page);
  Natura2000Image.Parent := Page.Surface;

  Natura2000Image.Bitmap.LoadFromFile(Natura2000BMPFileName);
  Natura2000Image.Top := ScaleY(8);
  Natura2000Image.AutoSize := True;

  Natura2000Radio := TNewRadioButton.Create(Page);
  Natura2000Radio.Parent := Page.Surface;
  Natura2000Radio.Caption := 'Natura 2000';
  Natura2000Radio.Checked := True;
  Natura2000Radio.Left := Natura2000Image.Width + ScaleX(5);
  Natura2000Radio.Top := ScaleY(8);

  EmeraldBMPFileName := ExpandConstant('{tmp}\emeraude_logo_small.bmp'); 
  ExtractTemporaryFile(ExtractFileName(EmeraldBMPFileName));
  
  EmeraldImage := TBitmapImage.Create(Page);
  EmeraldImage.Parent := Page.Surface;

  EmeraldImage.Bitmap.LoadFromFile(EmeraldBMPFileName);
  EmeraldImage.Top := ScaleY(58);
  EmeraldImage.AutoSize := True;

  EmeraldRadio := TNewRadioButton.Create(Page);
  EmeraldRadio.Parent := Page.Surface;
  EmeraldRadio.Caption := 'EMERALD';
  EmeraldRadio.Checked := False;
  EmeraldRadio.Left := Natura2000Image.Width + ScaleX(5);
  EmeraldRadio.Top := ScaleY(58);
 
end;
   
function NextButtonClick(CurPageID: Integer): Boolean;
begin
  if CurPageID = Page.ID then begin
      IsEmerald := EmeraldRadio.Checked;
      IsNatura2000 := Not IsEmerald;
  end;
Result := True;
end;

function IsEmeraldMode(): Boolean;
begin
  Result := IsEmerald;
end;

function IsNatura2000Mode(): Boolean;
begin
  Result := IsNatura2000;
end;

function GetAppID(const Value: string): string;
var
  AppID: string;
begin
  // check by using Assigned function, if the component you're trying to get a
  // value from exists; the Assigned will return False for the first time when
  // the GetAppID function will be called since even WizardForm not yet exists

    AppID := '872F6301-DCAB-4E5D-A7BC-BF97036EAF56';
    if IsEmeraldMode = True then 
      Result := AppID + '_emerald'
    else
      Result := AppID + '_n2k';
              
              
end;

function GetAppName(const Value: string): string;
begin

    if IsEmeraldMode = True then 
      Result := 'SDFManagerEmerald'
    else
      Result := 'SDFManager';

      
end;

[Files]
Source: "SDFManager.exe"; DestDir: "{app}"; Flags: ignoreversion

Source: "natura2000_logo.ico"; DestDir: "{app}\icons";  Check: IsNatura2000Mode
Source: "emeraude_logo.ico"; DestDir: "{app}\icons";  Check: IsEmeraldMode
Source: "natura2000_logo_small.bmp"; DestDir: "{app}\icons"; Flags: ignoreversion
Source: "emeraude_logo_small.bmp"; DestDir: "{app}\icons"; Flags: ignoreversion
Source: "SDFManager.l4j.ini"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\config\seed_sdf.properties"; DestDir: "{app}\config"; Flags: ignoreversion recursesubdirs createallsubdirs; Check: IsNatura2000Mode
Source: "..\config\seed_emerald.properties"; DestDir: "{app}\config"; Flags: ignoreversion recursesubdirs createallsubdirs; Check: IsEmeraldMode
Source: "..\config\*.xml"; DestDir: "{app}\config"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\database\*"; DestDir: "{app}\database"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\fonts\*"; DestDir: "{app}\fonts"; Flags: ignoreversion recursesubdirs createallsubdirs

Source: "..\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\lib\*"; Excludes: "mysql-connector-mxj-db-files-5.0.12.jar"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs

Source: "..\xsl\*"; DestDir: "{app}\xsl"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\xsd\*"; DestDir: "{app}\xsd"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
 
[Icons]
Name: "{group}\{code:GetAppName}"; Filename: "{app}\{#MyAppExeName}";IconFilename: {app}\icons\emeraude_logo.ico; Comment: "SDFManager"; Check: IsEmeraldMode
Name: "{group}\{code:GetAppName}"; Filename: "{app}\{#MyAppExeName}";IconFilename: {app}\icons\natura2000_logo.ico; Comment: "SDFManager"; Check: IsNatura2000Mode
Name: "{group}\{cm:ProgramOnTheWeb,{code:GetAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{code:GetAppName}}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\{code:GetAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon; IconFilename: {app}\icons\natura2000_logo.ico; Check: IsNatura2000Mode
Name: "{commondesktop}\{code:GetAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon; IconFilename: {app}\icons\emeraude_logo.ico; Check: IsEmeraldMode
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{code:GetAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

