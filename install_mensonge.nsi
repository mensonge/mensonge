Var JavaInstallationPath
!define JRE_VERSION "7.0"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=69474"


!define  "$INSTDIR/icon.png"
; The name of the installer
Name "LieLab"

; The file to write
OutFile "Setup.exe"

; The default installation directory
InstallDir $PROGRAMFILES\LieLab

; The text to prompt the user to enter a directory
DirText "Please choose a directory to which you'd like to install this application."
 
;--------------------------------


Section "Find Java" FINDJAVA    
   

    DetectTry1:  
    ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"  
    StrCmp $2 "" DetectTry2 JDK
    JDK:
    ReadRegStr $5 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$2" "JavaHome"  
    StrCmp $5 "" DetectTry2 GetValue  
   

 DetectTry2:
    StrCpy $1 "SOFTWARE\JavaSoft\Java Runtime Environment"  
    StrCpy $2 0  
    ReadRegStr $2 HKLM "$1" "CurrentVersion"  
    StrCmp $2 "" NoJava JRE
    JRE:
    ReadRegStr $5 HKLM "$1\$2" "JavaHome"  
    StrCmp $5 "" NoJava GetValue  

    GetValue:
    StrCpy $JavaInstallationPath $5
    ;Messagebox MB_OK "Javahome value: $JavaInstallationPath"
    Goto done

    NoJava:  
    # Messagebox MB_OK "No Java installation detected. Installing Java."  
    # Install Java
    MessageBox MB_ICONINFORMATION "Pytheas uses Java Runtime Environment ${JRE_VERSION}, it will now be downloaded and installed."
	StrCpy $2 "$TEMP\Java Runtime Environment.exe"
   	nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
   	Pop $R0 ;Get the return value
    	StrCmp $R0 "success" +3
      	MessageBox MB_ICONSTOP "Download failed: $R0"
      	Abort
    	ExecWait $2
    	Delete $2
     ; ExecWait "$INSTDIR\java\jre-6u26-windows-i586.exe"
    Goto DetectTry1

    done:   
    #$JavaInstallationPath should contain the system path to Java
SectionEnd  

; The stuff to install
Section "" ;No components page, name is not important
; Set output path to the installation directory.

SetOutPath $INSTDIR
MessageBox MB_OKCANCEL|MB_ICONINFORMATION "Vous allez installer LieLab ,$\n$\t souhaitez vous continuer ?" IDOK lbl_okay IDCANCEL lbl_ABORT
 lbl_ABORT:
             ; Abort the install
             ABORT
             GoTo lbl_okay
                     lbl_okay:
			
			; Put file there
			File LieLab.jar
			File /r lib
			File /r plugins
			File /r images
			WriteUninstaller $INSTDIR\Uninstall.exe
			CreateShortCut "$INSTDIR\LieLab.lnk" "$JavaInstallationPath\bin\javaw.exe" '-jar "$INSTDIR\LieLab.jar"' "$INSTDIR\\icon.ico" 
			CreateShortCut "$DESKTOP\LieLab.lnk" "$JavaInstallationPath\bin\javaw.exe" '-jar "$INSTDIR\LieLab.jar"' "$INSTDIR\\icon.ico" 
SectionEnd ; end the section 


Section "Uninstall"
MessageBox MB_OKCANCEL|MB_ICONINFORMATION "All existing files and folders under the LieLab installation directory will be removed.$\nThis includes any files and folders that have since been added after the installation of LieLab.$\n$\t$\tAre you sure you wish to continue?" IDOK lbl_del IDCANCEL lbl_ABORT
 lbl_ABORT:
             ; Abort the install
             ABORT
             GoTo lbl_del
 
                    lbl_del:
    			RMDir /r $INSTDIR
			Delete  "$DESKTOP\LieLab.lnk"
			Delete  "$INSTDIR\LieLab.lnk"
 
SectionEnd