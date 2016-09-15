
copyright_header = "////Wire//Copyright(C)2016WireSwissGmbH////Thisprogramisfreesoftware:youcanredistributeitand/ormodify//itunderthetermsoftheGNUGeneralPublicLicenseaspublishedby//theFreeSoftwareFoundation,eitherversion3oftheLicense,or//(atyouroption)anylaterversion.////Thisprogramisdistributedinthehopethatitwillbeuseful,//butWITHOUTANYWARRANTY;withouteventheimpliedwarrantyof//MERCHANTABILITYorFITNESSFORAPARTICULARPURPOSE.Seethe//GNUGeneralPublicLicenseformoredetails.////YoushouldhavereceivedacopyoftheGNUGeneralPublicLicense//alongwiththisprogram.Ifnot,seehttp://www.gnu.org/licenses/.//"

touched = git.added_files | git.modified_files
paths = touched.select { |f| f.end_with? ".h", ".m", ".swift", ".mm" }

paths.each do |p|
  next unless File.exist?(p)
  content = File.read(p)
  minified = content.delete "\s\n"

  # Warn if touched files are missing the copyright header
  warn("Missing copyright headers", file: p, line: 1) unless minified.include? copyright_header

  lines = content.split("\n")
  lines.each_with_index do |line, index|
    # warn if there are any TODOs, NSLogs or prints left in the touched files
    warn("TODO comment left", file: p, line: index + 1) if line.downcase =~ /\/\/\s?todo/
    warn("`NSLog` left", file: p, line: index + 1) if line.downcase =~ /\sNSLog(@"/
    warn("`print` left", file: p, line: index + 1) if line.downcase =~ /\sprint("/
  end
end

# Warn if there are no labels attached to the PR
warn "Please add labels to this PR" if github.pr_labels.count == 0

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
