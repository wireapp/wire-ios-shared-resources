# This file contains the fastlane.tools configuration
# You can find the documentation at https://docs.fastlane.tools
#
# For a list of all available actions, check out
#
#     https://docs.fastlane.tools/actions
#

# Uncomment the line if you want fastlane to automatically update itself
# update_fastlane

default_platform(:ios)

platform :ios do

	desc "Find first scheme of the project"
  	lane :find_scheme do
		sh("cd .. && xcodebuild -list -json | python -c 'import json,sys;obj=json.load(sys.stdin);sys.stdout.write(obj[\"project\"][\"schemes\"][0])'")
   	end

  	desc "Build for testing"
  	lane :build do
  		carthage(
  			command: "bootstrap",
  			platform: "iOS",
  			cache_builds: true,
  			new_resolver: true
  		)
  		xcodebuild(
  			analyze: true,
  			enable_code_coverage: true,
  			buildlog_path: "build",
  			derivedDataPath: "DerivedData",
  			xcargs: "clean build-for-testing",
  			scheme: find_scheme
  		)
  		xcpretty_report(
    		buildlog_path: 'build',
    		output_path: 'reports',
    		use_json_formatter: true
	  	)
  	end

  	desc "Test without building"
  	lane :test do
  		xcodebuild(
  			enable_code_coverage: true,
  			xcargs: "test-without-building",
  			scheme: find_scheme
  		)

  	end
end
