{
  description = "MicroPebble Android development environment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";

  outputs =
    { nixpkgs, ... }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };
      android = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [
          "33"
          "34"
          "36"
        ];
        buildToolsVersions = [ "35.0.0" ];
        includeCmake = false;
        includeEmulator = false;
        includeNDK = false;
        includeSystemImages = false;
      };
      androidSdk = android.androidsdk;
      jdk17 = pkgs.jdk17;
      jdk21 = pkgs.jdk21;
      runtimeLibraries = [
        pkgs.expat
        pkgs.fontconfig
        pkgs.freetype
        pkgs.libGL
        pkgs.zlib
      ];
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = [
          androidSdk
          android.platform-tools
          jdk21
          jdk17
          pkgs.dejavu_fonts
          pkgs.fontconfig
          pkgs.freetype
          pkgs.git
          pkgs.git-lfs
          pkgs.jujutsu
          pkgs.nushell
        ];

        ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
        ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
        FONTCONFIG_FILE = pkgs.makeFontsConf {
          fontDirectories = [ pkgs.dejavu_fonts ];
        };
        GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/35.0.0/aapt2 -Dorg.gradle.project.org.gradle.java.installations.paths=${jdk17.home},${jdk21.home}";
        JAVA_HOME = jdk21.home;
        JDK17_HOME = jdk17.home;
        LANG = "C.UTF-8";
        LC_ALL = "C.UTF-8";
        LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath runtimeLibraries;
      };
    };
}
