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

#-------------------------------------

    lane :release do |options|
      bump = options[:bump]
      unless ["major", "minor", "patch"].include? bump
        UI.user_error! "Invalid version bump"
      end
      # ensure_git_branch(
      #   branch: 'develop'
      # )
      # ensure_git_status_clean
      if options[:skip_tests]
          UI.important 'Skipping tests'
      else 
        # test
      end

      previous_version = read_version()
      bump_version_in_project(bump)
      message = release_details(previous_version)
      UI.important message
      git_commit(path: "*.xcconfig", message: message)
    end

    def should_create_release(version)
      if git_tag_exists(tag: version, remote: true) 
          UI.important "Tag #{version} already exists"
          return false
      end
      return true
    end
    
    def bump_version_in_project(bump)
      version = read_version()
      version[bump] += 1
      UI.important "New version: #{version}"
      write_version(version)
    end

    def changelog(version)
      changelog_from_git_commits(
        between: [flatten_version(version), "HEAD"],
        pretty: "%h | %s",
        merge_commit_filtering: "exclude_merges"
      )
    end

    def release_details(version)
      changelog = changelog(version)
      "Release #{flatten_version(version)}\n\n### Changelog\nCommit  | Details\n--------|------\n#{changelog}"
    end

    def parse_version(version)
      result = /^(\d+)\.(\d+)\.(\d+)$/.match(version)
      if result.nil? || result.captures.count != 3
        UI.user_error! "Invalid version bump"
      end
      { "major" => result[1].to_i, "minor" => result[2].to_i, "patch" => result[3].to_i }
    end

    def read_version()
      versionFilePath = "../Resources/Configurations/version.xcconfig"

      data = File.read(versionFilePath)
      if data.nil?
        UI.user_error! "Cannot read version from #{versionFilePath}"
      end

      matches = /^CURRENT_PROJECT_VERSION = (?<version>[\d\.]+)$/.match(data)
      if matches.nil?
        UI.user_error! "Invalid CURRENT_PROJECT_VERSION in #{versionFilePath}"
      end

      parse_version(matches[:version])
    end

    def flatten_version(version)
      "#{version["major"]}.#{version["minor"]}.#{version["patch"]}"
    end

    def write_version(version)
      versionFilePath = "../Resources/Configurations/version.xcconfig"
      contents = <<~EOS
        MAJOR_VERSION = #{version["major"]}
        CURRENT_PROJECT_VERSION = #{flatten_version(version)}
      EOS
      File.write(versionFilePath, contents)
    end


end
