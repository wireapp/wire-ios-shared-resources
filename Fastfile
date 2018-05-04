default_platform(:ios)

platform :ios do

  	desc "Build for testing"
  	lane :build do
  		carthage(
  			command: "bootstrap",
  			platform: "iOS",
  			cache_builds: true,
  			new_resolver: true
  		)
  		scan(
        build_for_testing: true,
        xcargs: "analyze",
        clean: true,
        devices: ["iPhone 7"],
        code_coverage: true,
        output_directory: "build",
        output_types: "junit",
  			buildlog_path: "build",
  			derived_data_path: "DerivedData",
        formatter: "xcpretty-json-formatter"
  		)

  	end

  	desc "Test without building"
  	lane :test do
        scan(
          test_without_building: true,
          devices: ["iPhone 7"],
          code_coverage: true,
          output_directory: "test",
          output_types: "junit",
          buildlog_path: "test",
          derived_data_path: "DerivedData"
      )

      trainer(output_directory: "build")
  	end
end
