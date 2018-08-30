# Skip Danger for work-in-progress PRs
if github.pr_title.include?("[WIP]") || github.pr_title.include?("WIP:")
  return
end

def copyright_header(year = Time.new.year)
  return "////Wire//Copyright(C)#{year}WireSwissGmbH////Thisprogramisfreesoftware:youcanredistributeitand/ormodify//itunderthetermsoftheGNUGeneralPublicLicenseaspublishedby//theFreeSoftwareFoundation,eitherversion3oftheLicense,or//(atyouroption)anylaterversion.////Thisprogramisdistributedinthehopethatitwillbeuseful,//butWITHOUTANYWARRANTY;withouteventheimpliedwarrantyof//MERCHANTABILITYorFITNESSFORAPARTICULARPURPOSE.Seethe//GNUGeneralPublicLicenseformoredetails.////YoushouldhavereceivedacopyoftheGNUGeneralPublicLicense//alongwiththisprogram.Ifnot,seehttp://www.gnu.org/licenses/.//"
end


def filterFiles(files)
  return files.select { |f| f.end_with? ".h", ".m", ".swift", ".mm" }
end

def print_calling_coverage(coverage)
  targets = coverage["targets"]
  first_target = targets[0]
  target_files = first_target["files"]

  calling_files = target_files.select { |file| !!(file["path"] =~ /calling/i) }
  unless calling_files.empty? 
      lines_covered = calling_files.reduce([0, 0]) { |sum, file| [sum[0] + file["executableLines"], sum[1] + file["coveredLines"]] }
      test_coverage = if lines_covered[0] != 0 then lines_covered[1].to_f / lines_covered[0] else 0.0 end
      total = lines_covered[0]
      covered = lines_covered[1]
      coverage_percents = '%.2f' % (test_coverage * 100)
      message "Calling test coverage: #{coverage_percents}% [#{covered} of #{total} lines in #{calling_files.length} files]"
  end

end

added_paths = filterFiles(git.added_files)
touched_paths = filterFiles(git.added_files | git.modified_files)

touched_paths.each do |p|
  next unless File.exist?(p)
  content = File.read(p)
 
  lines = content.split("\n")
  lines.each_with_index do |line, index|
    # warn if there are any TODOs, NSLogs or prints left in the touched files
    warn("`TODO` comment left", file: p, line: index + 1) if line.downcase =~ /\/\/\s?todo/
    warn("`FIXME` comment left", file: p, line: index + 1) if line.downcase =~ /\/\/\s?fixme/
    warn("`NSLog` left", file: p, line: index + 1) if line.downcase =~ /\sNSLog\(@"/
    warn("`print` left", file: p, line: index + 1) if line.downcase =~ /\sprint\("/
  end
end

added_paths.each do |p|
  next unless File.exist?(p)
  minified = File.read(p).delete "\s\n"
  
  if minified.include? copyright_header(Time.new.year - 1)
    warn("Copyright header is from last year", file: p, line: 3)
  else 
    warn("Copyright header is missing or in wrong format", file: p, line: 1) unless minified.include? copyright_header
  end
 
end

# Warn if the Cartfile.resolved points to a commit SHA instead of a tag
cartfile_name = 'Cartfile.resolved'

if File.exists? cartfile_name
  cartfile = File.read(cartfile_name)

  cartfile.lines.each do |line|
    elements = line.delete('\"').split(' ')
    next unless elements.count == 3
    framework, version = elements.drop(1)
    warn "`#{framework}` is still pinned to `#{version}`" if version.length == 40
  end
end

xcode_summary.report 'build/reports/errors.json'

# Calling code coverage
Open3.popen2("xcrun xccov view DerivedData/Logs/Test/*.xccovreport --json") {|i,o,t|
  output = o.gets
  if t.value.success? 
    json = JSON.parse(output)
    print_calling_coverage(json)
  end
}

