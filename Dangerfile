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

added_paths = filterFiles(git.added_files)
touched_paths = filterFiles(git.added_files | git.modified_files)

touched_paths.each do |p|
  next unless File.exist?(p)
  content = File.read(p)
  minified = content.delete "\s\n"

  unless minified.include? copyright_header(Time.new.year - 1)
    warn("Copyright header is missing or in wrong format", file: p, line: 1) unless minified.include? copyright_header
  end
 
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
  # Warn if added files do not have expected copyright header
  warn("Your copyright header is so last year!", file: p, line: 3) if minified.include? copyright_header(Time.new.year - 1)  
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

xcode_summary.ignored_files = '**/Pods/**'

xcode_summary.report 'xcodebuild.json'
